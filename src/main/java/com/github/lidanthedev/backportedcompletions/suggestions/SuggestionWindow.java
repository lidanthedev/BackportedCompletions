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
    private int suggestionIndex = 0;
    private Consumer<String> setInputFieldText;
    private Supplier<String> getInputFieldText;
    private String selectedSuggestion = "";
    private String lastText = "";

    public SuggestionWindow(GuiChat gui) {
        this.gui = gui;
    }

    public void render(int mouseX, int mouseY, float partialTicks) {
        if (suggestions.isEmpty()) return;
        if (suggestionIndex >= suggestions.size()) suggestionIndex = 0;
        int boxHeight = Math.min(suggestions.size(), 10) * 10 - 8;
        int maxWidth = 0;
        for (String s : suggestions) {
            maxWidth = Math.max(maxWidth, Minecraft.getMinecraft().fontRendererObj.getStringWidth(s));
        }
        GuiChat.drawRect(2, gui.height - 26 - boxHeight, maxWidth + 6, gui.height - 16, -805306368);
        // Draw the text inside the rectangle
        int offset = 0;
        if (suggestionIndex > 9){
            offset = (suggestionIndex - 9);
        }
        int suggestionCount = 0;
        boolean moreAtBottom = false;
        boolean moreAtTop = false;
        for (String suggestion : suggestions) {
            int realIndex = suggestions.indexOf(suggestion);
            if (realIndex < offset) continue;
            if (suggestionCount >= 10) {
                moreAtBottom = true;
                break;
            };
            int color = realIndex == this.suggestionIndex ? -256 : -5592406;
            Minecraft.getMinecraft().fontRendererObj.drawString(suggestion, 4, gui.height - 24 - boxHeight + (suggestionCount * 10), color);
            suggestionCount++;
        }
        String dots = Strings.repeat('.', maxWidth / 2 + 1);
        if (moreAtBottom) {
            Minecraft.getMinecraft().fontRendererObj.drawString(dots, 4, gui.height - 24 - boxHeight + (suggestionCount * 10) - 6, 0xFFFFFF);
        }
        if (suggestionIndex > 0 && suggestionIndex > 9) {
            moreAtTop = true;
            Minecraft.getMinecraft().fontRendererObj.drawString(dots, 4, gui.height - 24 - boxHeight - 6, 0xFFFFFF);
        }
        selectedSuggestion = suggestions.get(suggestionIndex);
    }

    public void onMouseClick(int mouseX, int mouseY, int mouseButton) {

    }

    public void onKeyTypedPre(char typedChar, int keyCode, CallbackInfo ci) {
        if (suggestions.isEmpty()) return;
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
        if (keyCode == 15 || keyCode == 78) { // Enter key or tab
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
                return attemptSelectSuggestion(suggestionDown());
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

    public boolean mouseScrolled(double amount) {
        return true;
    }


    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions.stream().sorted().collect(Collectors.toList());
    }
}
