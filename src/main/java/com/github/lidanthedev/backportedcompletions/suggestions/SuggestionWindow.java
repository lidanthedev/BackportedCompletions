package com.github.lidanthedev.backportedcompletions.suggestions;

import com.github.lidanthedev.backportedcompletions.config.ModConfig;
import com.mojang.authlib.GameProfile;
import joptsimple.internal.Strings;
import lombok.Data;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraftforge.client.ClientCommandHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Data
public class SuggestionWindow {
    private static final Logger log = LogManager.getLogger(SuggestionWindow.class);
    private GuiChat gui;
    private List<String> suggestions = new ArrayList<>();
    private List<String> lastSuggestions = new ArrayList<>(); // last suggestions returned from server
    private List<String> shownSuggestions = new ArrayList<>();
    private int suggestionIndex = 0;
    private final Consumer<String> setInputFieldText;
    private final Supplier<String> getInputFieldText;
    private final Runnable requestAutocomplete;
    private String selectedSuggestion = "";
    private int scrollOffset = 0;
    private int lastMouseX = 0, lastMouseY = 0;
    private int boxWidth = 0;
    private int boxHeight = 0;
    private boolean matchStartOnly = ModConfig.matchStartOnly;
    private boolean caseSensitive = ModConfig.caseSensitive;


    public SuggestionWindow(GuiChat gui, Consumer<String> setInputFieldText, Supplier<String> getInputFieldText, Runnable requestAutocomplete) {
        this.gui = gui;
        this.setInputFieldText = setInputFieldText;
        this.getInputFieldText = getInputFieldText;
        this.requestAutocomplete = requestAutocomplete;
    }

    public void render(int mouseX, int mouseY, float partialTicks) {
        if (suggestions.isEmpty()) return;
        suggestionIndex = Math.min(suggestionIndex, suggestions.size() - 1);
        boxHeight = Math.min(suggestions.size(), 10) * 10 - 8;
        boxWidth = getMaxWidth();
        drawBackground(boxHeight, boxWidth);
        drawSuggestions();
        drawScrollDots();
        updateSelectedSuggestion();
        updateHoveredSuggestion(mouseX, mouseY);
    }

    private void updateHoveredSuggestion(int mouseX, int mouseY) {
        if (lastMouseX == mouseX && lastMouseY == mouseY) return;
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        String hovered = getHoveredSuggestion(mouseX, mouseY);
        if (!hovered.isEmpty()){
            selectedSuggestion = hovered;
            suggestionIndex = suggestions.indexOf(hovered);
        }
    }

    private void drawBackground(int boxHeight, int maxWidth) {
        GuiChat.drawRect(2, gui.height - 26 - boxHeight, maxWidth + 6, gui.height - 16, -805306368);
    }

    private void drawScrollDots() {
        String dots = Strings.repeat('.', boxWidth / 2 + 1);
        int baseY = gui.height - 24 - boxHeight;

        if (scrollOffset > 0) {
            Minecraft.getMinecraft().fontRendererObj.drawString(dots, 4, baseY - 8, 0xFFFFFF);
        }

        if (scrollOffset + 10 < suggestions.size()) {
            int dotY = baseY + shownSuggestions.size() * 10 - 6;
            Minecraft.getMinecraft().fontRendererObj.drawString(dots, 4, dotY, 0xFFFFFF);
        }
    }

    private void drawSuggestions() {
        shownSuggestions.clear();
        int suggestionCount = 0;

        for (int i = scrollOffset; i < suggestions.size(); i++) {
            if (suggestionCount >= 10) break;

            String suggestion = suggestions.get(i);
            int color = (i == suggestionIndex) ? -256 : -5592406;
            Minecraft.getMinecraft().fontRendererObj.drawString(
                    suggestion,
                    4,
                    gui.height - 24 - boxHeight + (suggestionCount * 10),
                    color
            );
            shownSuggestions.add(suggestion);
            suggestionCount++;
        }
    }

    private void updateSelectedSuggestion() {
        if (!suggestions.isEmpty()) {
            selectedSuggestion = suggestions.get(suggestionIndex);
        }
    }


    private int getMaxWidth() {
        int maxWidth = 0;
        for (String s : suggestions) {
            maxWidth = Math.max(maxWidth, getWidthCached(s));
        }
        return maxWidth;
    }

    public String getHoveredSuggestion(int mouseX, int mouseY) {
        if (suggestions.isEmpty()) return "";
        int boxHeight = Math.min(suggestions.size(), 10) * 10 - 8;
        int maxWidth = getMaxWidth();
        if (mouseX >= 2 && mouseX <= maxWidth + 6 && mouseY >= gui.height - 26 - boxHeight && mouseY <= gui.height - 16) {
            int suggestionCount = 0;
            for (String suggestion : shownSuggestions) {
                if (mouseY >= gui.height - 24 - boxHeight + (suggestionCount * 10) && mouseY <= gui.height - 24 - boxHeight + ((suggestionCount + 1) * 10)) {
                    return suggestion;
                }
                suggestionCount++;
            }
        }
        return "";
    }

    public void onMouseClick(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
        if (shownSuggestions.isEmpty()) return;
        String hovered = getHoveredSuggestion(mouseX, mouseY);
        if (hovered.isEmpty()) return;
        attemptSelectSuggestion(hovered, true);
        ci.cancel();
    }

    public void onKeyTypedPre(char typedChar, int keyCode, CallbackInfo ci) {
        String text = getInputFieldText.get();
        if (suggestions.isEmpty()) {
            if (keyCode == Keyboard.KEY_TAB){
                requestAutocomplete.run();
                if (text.isEmpty()){
                    setSuggestions(Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap().stream()
                            .map(NetworkPlayerInfo::getGameProfile)
                            .map(GameProfile::getName)
                            .collect(Collectors.toList()));
                }
                ci.cancel();
            }
            return;
        }
        if (keyCode == Keyboard.KEY_ESCAPE){
            // Escape key
            suggestions.clear();
            ci.cancel();
        }
        if (keyCode == Keyboard.KEY_UP) { // Up arrow key
            suggestionUp();
            ci.cancel();
        } else if (keyCode == Keyboard.KEY_DOWN) { // Down arrow key
            suggestionDown();
            ci.cancel();
        }
        if (keyCode == Keyboard.KEY_TAB) { // Enter tab
            if (attemptSelectSuggestion()) return;
            ci.cancel();
        }
    }

    private boolean attemptSelectSuggestion() {
        return attemptSelectSuggestion(selectedSuggestion, false);
    }

    private boolean attemptSelectSuggestion(String suggestion, boolean force) {
        if (suggestion.isEmpty()) return true;
        log.info("Selected suggestion: {}", suggestion);
        String finalText = suggestion;
        if (getInputFieldText != null) {
            String inputText = getInputFieldText.get();
            String fixedInputText = inputText + "a"; // extra char to prevent empty string
            String[] split = fixedInputText.split(" ");
            String allBeforeLastWord = String.join(" ", Arrays.copyOf(split, split.length - 1));
            String addSpace = allBeforeLastWord.isEmpty() ? "" : " ";
            finalText = allBeforeLastWord + addSpace + suggestion;
            if (finalText.equals(inputText) && !force) {
                String newSuggestion;
                if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
                    newSuggestion = suggestionUp();
                else
                    newSuggestion = suggestionDown();
                if (newSuggestion.equals(suggestion)) return true;
                return attemptSelectSuggestion(newSuggestion, true);
            }
        }
        if (setInputFieldText != null)
            setInputFieldText.accept(finalText);
        return false;
    }

    private String suggestionUp() {
        suggestionIndex--;
        if (suggestionIndex < 0) {
            suggestionIndex = suggestions.size() - 1;
        }
        selectedSuggestion = suggestions.get(suggestionIndex);
        fixOffset();
        return selectedSuggestion;
    }

    private String suggestionDown() {
        suggestionIndex++;
        if (suggestionIndex >= suggestions.size()) {
            suggestionIndex = 0;
        }
        selectedSuggestion = suggestions.get(suggestionIndex);
        fixOffset();
        return selectedSuggestion;
    }

    public void onKeyTypedPost(char typedChar, int keyCode, CallbackInfo ci) {
        if (typedChar == 0) return;
        if (keyCode == Keyboard.KEY_SPACE){
            suggestions.clear();
            lastSuggestions.clear();
            suggestionIndex = 0;
            scrollOffset = 0;
            selectedSuggestion = "";
            return;
        }
        syncSuggestions();
    }

    public String getCurrentInputAtCursor() {
        if (getInputFieldText == null) return "";
        String inputText = getInputFieldText.get();
        String fixedInputText = inputText + "a"; // extra char to prevent empty string
        if (inputText.isEmpty()) return "";
        String[] split = fixedInputText.split(" ");
        String last = split[split.length - 1];
        return last.substring(0, last.length() - 1);
    }

    private void syncSuggestions() {
        if (getInputFieldText == null) return;

        String inputText = getCurrentInputAtCursor();
        if (inputText.isEmpty()) return;

        final String filter = caseSensitive ? inputText : inputText.toLowerCase();

        this.suggestions = lastSuggestions.stream()
                .filter(s -> {
                    if (s.isEmpty()) return false;

                    String target = caseSensitive ? s : s.toLowerCase();
                    return matchStartOnly
                            ? target.startsWith(filter)
                            : target.contains(filter);
                })
                .distinct()
                .collect(Collectors.toList());

        suggestionIndex = 0;
        fixOffset();
    }


    public void mouseScrolled(double amount, CallbackInfo ci) {
        if (suggestions.isEmpty()) return;
        // check if in the suggestion window
        int boxHeight = Math.min(suggestions.size(), 10) * 10 - 8;
        int maxWidth = getMaxWidth();
        // if not in box return
        if (lastMouseX < 2 || lastMouseX > maxWidth + 6 || lastMouseY < gui.height - 26 - boxHeight || lastMouseY > gui.height - 16) {
            return;
        }
        if (amount > 1){
            scrollOffset--;
            if (scrollOffset < 0) {
                scrollOffset = 0;
            }
            ci.cancel();
        }
        if (amount < -1){
            scrollOffset++;
            if (scrollOffset > suggestions.size() - 10) {
                scrollOffset = Math.max(suggestions.size() - 10, 0);
            }
            ci.cancel();
        }
    }

    public void setSuggestions(List<String> suggestions) {
        List<String> allSuggestions = new ArrayList<>(suggestions);
        if (getInputFieldText != null) {
            String inputText = getInputFieldText.get();
            if (inputText.startsWith("/")) {
                String[] latestClientAutoComplete = ClientCommandHandler.instance.latestAutoComplete;
                if (latestClientAutoComplete != null)
                    allSuggestions.addAll(Arrays.asList(latestClientAutoComplete));
            }
        }
        List<String> processed = allSuggestions.stream()
                .map(this::stripColor)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        this.suggestions = new ArrayList<>(processed);
        this.lastSuggestions = new ArrayList<>(processed);

        this.scrollOffset = 0;
        syncSuggestions();
    }

    public void fixOffset(){
        if (suggestionIndex > (scrollOffset + 9) && suggestionIndex > scrollOffset){
            scrollOffset = (suggestionIndex - 9);
        }
        else if (suggestionIndex < scrollOffset){
            scrollOffset = suggestionIndex;
        }
    }

    public String stripColor(String str) {
        return str.replaceAll("§[0-9a-fk-or]", "");
    }

    Map<String, Integer> widthCache = new HashMap<>();
    int getWidthCached(String s) {
        if (s.isEmpty()) return 0;
        return widthCache.computeIfAbsent(s, s1 -> Minecraft.getMinecraft().fontRendererObj.getStringWidth(s1));
    }
}
