package com.github.lidanthedev.backportedcompletions.mixin;

import com.github.lidanthedev.backportedcompletions.suggestions.SuggestionWindow;
import com.google.common.collect.ObjectArrays;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.client.ClientCommandHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;

@Mixin(GuiChat.class)
public class MixinGuiChat {
    private static final Logger log = LogManager.getLogger(MixinGuiChat.class);
    private SuggestionWindow suggestionWindow;
    private String lastText = "";

    @Shadow
    private List<String> foundPlayerNames;

    @Shadow
    private boolean playerNamesFound;

    @Shadow
    protected GuiTextField inputField;
    @Shadow
    private int autocompleteIndex;
    @Shadow
    private boolean waitingOnAutocomplete;

    @Shadow
    private void sendAutocompleteRequest(String p_146405_1_, String p_146405_2_) {}

    @Inject(method = "initGui", at = @At("RETURN"))
    public void initGui(CallbackInfo ci) {
        GuiChat thisGuiChat = (GuiChat) (Object) this;
        suggestionWindow = new SuggestionWindow(thisGuiChat);
        suggestionWindow.setSetInputFieldText(this::changeInputFieldText);
        suggestionWindow.setGetInputFieldText(this::getInputFieldText);
    }

    @Inject(method = "onAutocompleteResponse", at = @At("HEAD"), cancellable = true)
    public void onAutocompleteResponse(String[] p_146406_1_, CallbackInfo ci) {
//        log.info("onAutocompleteResponse {}", Arrays.toString(p_146406_1_));
        if (this.waitingOnAutocomplete) {
            this.playerNamesFound = false;
            this.foundPlayerNames.clear();
            String[] complete = ClientCommandHandler.instance.latestAutoComplete;
            if (complete != null) {
                p_146406_1_ = ObjectArrays.concat(complete, p_146406_1_, String.class);
            }
            for (String s : p_146406_1_) {
                if (s.length() <= 0) continue;
                this.foundPlayerNames.add(s);
            }
            if (!this.foundPlayerNames.isEmpty()) {
                this.playerNamesFound = true;
            }
            this.suggestionWindow.setSuggestions(this.foundPlayerNames);
            ci.cancel();
        }
    }

    @Inject(method = "keyTyped", at = @At("HEAD"), cancellable = true)
    public void keyTypedPre(char typedChar, int keyCode, CallbackInfo ci) {
        if (this.foundPlayerNames.isEmpty()) {
            return;
        }
        suggestionWindow.onKeyTypedPre(typedChar, keyCode, ci);
    }

    @Inject(method = "keyTyped", at = @At("RETURN"))
    public void keyTypedPost(char typedChar, int keyCode, CallbackInfo ci) {
        if (ci.isCancelled()) return;
        String text = inputField.getText();
        if (keyCode == 200 || keyCode == 208) { // Up arrow key or Down arrow key
            suggestionWindow.onKeyTypedPost(typedChar, keyCode, ci);
            return;
        }
        if (text != null && !text.equals(lastText) && (text.startsWith("/") || keyCode == 15)) {
            this.sendCompletionRequest();
        }
        if (text != null){
            this.lastText = text;
        }
        if (text != null && text.isEmpty() && keyCode != 15){
            this.foundPlayerNames.clear();
            this.suggestionWindow.setSuggestions(this.foundPlayerNames);
            this.suggestionWindow.setSuggestionIndex(0);
            this.autocompleteIndex = 0;
            this.playerNamesFound = false;
            this.suggestionWindow.setSuggestions(this.foundPlayerNames);
        }
        this.suggestionWindow.onKeyTypedPost(typedChar, keyCode, ci);
    }

    @Inject(method = "drawScreen", at = @At("HEAD"))
    public void drawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (this.foundPlayerNames.isEmpty()) {
            return;
        }
        suggestionWindow.render(mouseX, mouseY, partialTicks);
    }

    @Inject(method = "handleMouseInput", at= @At("HEAD"))
    public void handleMouseInput(CallbackInfo ci) {
        int eventDWheel = Mouse.getEventDWheel();
        suggestionWindow.mouseScrolled(eventDWheel);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"))
    public void mouseClicked(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
        suggestionWindow.onMouseClick(mouseX, mouseY, mouseButton);
    }

    private void sendCompletionRequest(){
        int i = this.inputField.func_146197_a(-1, this.inputField.getCursorPosition(), false);
        this.foundPlayerNames.clear();
        this.autocompleteIndex = 0;
        suggestionWindow.setSuggestionIndex(0);
        suggestionWindow.setSuggestions(this.foundPlayerNames);
        String s = this.inputField.getText().substring(i).toLowerCase();
        String s1 = this.inputField.getText().substring(0, this.inputField.getCursorPosition());
        this.sendAutocompleteRequest(s1, s);
    }

    public void changeInputFieldText(String text) {
        this.inputField.setText(text);
    }

    public String getInputFieldText() {
        return this.inputField.getText();
    }
}
