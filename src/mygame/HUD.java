package mygame;

import com.jme3.app.state.AbstractAppState;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioRenderer;
import com.jme3.input.InputManager;
import com.jme3.renderer.ViewPort;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import com.jme3.niftygui.NiftyJmeDisplay;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;

/**
 *
 * @author Amir, Lukas, Daniel
 */
public class HUD extends AbstractAppState implements ScreenController {
    public int CurrentTower = -1;

    Element Welle;
    Element Money;
    Element GameOver;
    Nifty nifty;
    NiftyJmeDisplay niftyDisplay;
    MainGame main;

    public HUD(MainGame main, final AssetManager assetManager, final InputManager inputManager, final AudioRenderer audioRenderer, final ViewPort guiViewPort) {
        this.main = main;
        niftyDisplay = new NiftyJmeDisplay(assetManager, inputManager, audioRenderer, guiViewPort);
        guiViewPort.addProcessor(niftyDisplay);
        nifty = niftyDisplay.getNifty();

        nifty.fromXml("Interface/IngameUI.xml", "sidebar", this);
    }

    public void bind(Nifty nifty, Screen screen) {
        if (!screen.getScreenId().equals("sidebar"))
            return;

        Welle = screen.getRootElement().findElementByName("layer")
                .findElementByName("ingameStats")
                .findElementByName("Welle");
        Money = screen.getRootElement().findElementByName("layer")
                .findElementByName("ingameStats")
                .findElementByName("Money");
        GameOver = screen.getRootElement().findElementByName("layer")
                .findElementByName("GameOver");
        GameOver.setVisible(false);
    }

    public void onStartScreen() { }
    public void onEndScreen() { }

    public void setMoney(int money) {
        Money.getRenderer(TextRenderer.class).setText("Geld: " + money);
    }

    public void setzeWelle(int welle) {
        Welle.getRenderer(TextRenderer.class).setText("Welle: " + welle);
    }

    public void GameOver() {
        GameOver.setVisible(true);
    }

    public void placeTower(String type) {
        CurrentTower = -1;

        switch (Integer.parseInt(type)) {
            case 0:
                CurrentTower = Tower.TYPE_MARINE;
                break;
            case 1:
                CurrentTower = Tower.TYPE_POLICE;
                break;
            case 2:
                CurrentTower = Tower.TYPE_UNICORN;
                break;
            case 3:
                CurrentTower = Tower.TYPE_MURICA;
                break;
        }

        if (CurrentTower == -1)
            throw new IllegalArgumentException("Failed to parse given towertype in placeTower.");

        main.CreateTowerPreview();
    }

    public void selectUpgrade(String type) {
        // 0 == cap
        // 1 == toupe
        // 2 == flag
    }

    public void openMenu() {
        
    }

    public void pause() {
        Main.instance.isRunning = false;
        Main.instance.triggered = true;
        nifty.fromXml("Interface/IngameUI.xml", "pause", this);
    }

    public void nextWave() {
        main.SkipWave();
    }

    public void openSkilltree() {
        
    }

    public void resume() {
        Main.instance.isRunning = true;
        Main.instance.triggered = true;
        nifty.fromXml("Interface/IngameUI.xml", "sidebar", this);
    }
}
