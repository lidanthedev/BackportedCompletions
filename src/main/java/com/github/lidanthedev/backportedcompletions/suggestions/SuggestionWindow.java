package com.github.lidanthedev.backportedcompletions.suggestions;

import joptsimple.internal.Strings;
import lombok.Data;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Data
public class SuggestionWindow {
    private static final Logger log = LogManager.getLogger(SuggestionWindow.class);
    private GuiChat gui;
    private List<String> suggestions = new ArrayList<>();
    private List<String> shownSuggestions = new ArrayList<>();
    private int suggestionIndex = 0;
    private Consumer<String> setInputFieldText;
    private Supplier<String> getInputFieldText;
    private String selectedSuggestion = "";
    private String lastText = "";
    private int suggestionOffset = 0;
    private int lastMouseX = 0, lastMouseY = 0;
    private int boxWidth = 0;
    private int boxHeight = 0;

    public SuggestionWindow(GuiChat gui) {
        this.gui = gui;
    }

    public void render(int mouseX, int mouseY, float partialTicks) {
        if (suggestions.isEmpty()) return;
        if (suggestionIndex >= suggestions.size()) suggestionIndex = 0;
        int boxHeight = Math.min(suggestions.size(), 10) * 10 - 8;
        int maxWidth = getMaxWidth();
        GuiChat.drawRect(2, gui.height - 26 - boxHeight, maxWidth + 6, gui.height - 16, -805306368);
        // Draw the text inside the rectangle
        if (suggestionIndex > (suggestionOffset + 9) && suggestionIndex > suggestionOffset){
            suggestionOffset = (suggestionIndex - 9);
        }
        else if (suggestionIndex < suggestionOffset){
            suggestionOffset = suggestionIndex;
        }
        int suggestionCount = 0;
        boolean moreAtBottom = false;
        shownSuggestions.clear();
        for (String suggestion : suggestions) {
            int realIndex = suggestions.indexOf(suggestion);
            if (realIndex < suggestionOffset) continue;
            if (suggestionCount >= 10) {
                moreAtBottom = true;
                break;
            };
            int color = realIndex == this.suggestionIndex ? -256 : -5592406;
            Minecraft.getMinecraft().fontRendererObj.drawString(suggestion, 4, gui.height - 24 - boxHeight + (suggestionCount * 10), color);
            shownSuggestions.add(suggestion);
            suggestionCount++;
        }
        String dots = Strings.repeat('.', maxWidth / 2 + 1);
        if (moreAtBottom) {
            Minecraft.getMinecraft().fontRendererObj.drawString(dots, 4, gui.height - 24 - boxHeight + (suggestionCount * 10) - 6, 0xFFFFFF);
        }
        if (suggestionOffset > 0) {
            Minecraft.getMinecraft().fontRendererObj.drawString(dots, 4, gui.height - 24 - boxHeight - 8, 0xFFFFFF);
        }
        selectedSuggestion = suggestions.get(suggestionIndex);
        // mouse hover
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        String hovered = getHoveredSuggestion(mouseX, mouseY);
        if (!hovered.isEmpty()){
            selectedSuggestion = hovered;
            suggestionIndex = suggestions.indexOf(hovered);
        }
    }

    private int getMaxWidth() {
        int maxWidth = 0;
        for (String s : suggestions) {
            maxWidth = Math.max(maxWidth, Minecraft.getMinecraft().fontRendererObj.getStringWidth(s));
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

    public void onMouseClick(int mouseX, int mouseY, int mouseButton) {
        if (shownSuggestions.isEmpty()) return;
        int boxHeight = Math.min(suggestions.size(), 10) * 10 - 8;
        getMaxWidth();
        String hovered = getHoveredSuggestion(mouseX, mouseY);
        if (hovered.isEmpty()) return;
        attemptSelectSuggestion(hovered);
    }

    public void onKeyTypedPre(char typedChar, int keyCode, CallbackInfo ci) {
        if (suggestions.isEmpty()) {
            if (keyCode == 15){
                ci.cancel();
            }
            return;
        }
        if (keyCode == 1){
            // Escape key
            suggestions.clear();
            ci.cancel();
        }
        if (keyCode == 200) { // Up arrow key
            suggestionUp();
            ci.cancel();
        } else if (keyCode == 208) { // Down arrow key
            suggestionDown();
            ci.cancel();
        }
        if (keyCode == 15) { // Enter tab
            if (attemptSelectSuggestion()) return;
            ci.cancel();
        }
    }

    private boolean attemptSelectSuggestion() {
        return attemptSelectSuggestion(selectedSuggestion);
    }

    private boolean attemptSelectSuggestion(String suggestion){
        log.info("attempt to select suggestion");
        if (suggestion.isEmpty()) return true;
        log.info("Selected suggestion: {}", suggestion);
        String finalText = suggestion;
        if (getInputFieldText != null) {
            String inputText = getInputFieldText.get();
            String fixedInputText = inputText + "a"; // extra char to prevent empty string
            String[] split = fixedInputText.split(" ");
            String allBeforeLastWord = "";
            for (int i = 0; i < split.length - 1; i++) {
                allBeforeLastWord += split[i] + " ";
            }
            allBeforeLastWord = allBeforeLastWord.trim();
            String addSpace = allBeforeLastWord.isEmpty() ? "" : " ";
            finalText = allBeforeLastWord + addSpace + suggestion;
            if (finalText.equals(inputText)) {
                String newSuggestion = suggestionDown();
                if (newSuggestion.equals(suggestion)) return true;
                return attemptSelectSuggestion(newSuggestion);
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
        return selectedSuggestion;
    }

    private String suggestionDown() {
        suggestionIndex++;
        if (suggestionIndex >= suggestions.size()) {
            suggestionIndex = 0;
        }
        selectedSuggestion = suggestions.get(suggestionIndex);
        return selectedSuggestion;
    }

    public void onKeyTypedPost(char typedChar, int keyCode, CallbackInfo ci) {

    }

    public void mouseScrolled(double amount) {
        if (suggestions.isEmpty()) return;
        // check if in the suggestion window
        int boxHeight = Math.min(suggestions.size(), 10) * 10 - 8;
        int maxWidth = getMaxWidth();
        // if not in box return
        if (lastMouseX < 2 || lastMouseX > maxWidth + 6 || lastMouseY < gui.height - 26 - boxHeight || lastMouseY > gui.height - 16) {
            return;
        }
        if (amount > 1){
            suggestionOffset--;
            if (suggestionOffset < 0) {
                suggestionOffset = 0;
            }
        }
        if (amount < -1){
            suggestionOffset++;
            if (suggestionOffset > suggestions.size() - 10) {
                suggestionOffset = suggestions.size() - 10;
            }
        }
    }


    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions.stream().sorted().collect(Collectors.toList());
        this.suggestionOffset = 0;
    }
}
