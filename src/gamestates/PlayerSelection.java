package gamestates;

import entities.PlayerCharacter;
import main.Game;
import ui.MenuButton;
import utilz.LoadSave;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import static utilz.Constants.ANI_SPEED;
import static utilz.Constants.PlayerConstants.IDLE;

public class PlayerSelection extends State implements Statemethods {

    private BufferedImage backgroundImg, backgroundImgPink;
    private int menuX, menuY, menuWidth, menuHeight;
    private MenuButton playButton;
    private int playerIndex = 0;

    private CharacterAnimation[] characterAnimations;

    public PlayerSelection(Game game) {
        super(game);
        loadButtons();
        loadBackground();
        backgroundImgPink = LoadSave.GetSpriteAtlas(LoadSave.MENU_BACKGROUND_IMG);
        loadCharAnimations();
    }

    private void loadCharAnimations() {
        characterAnimations = new CharacterAnimation[3];
        int i = 0;
        characterAnimations[i++] = new CharacterAnimation(PlayerCharacter.WOMAN);
        characterAnimations[i++] = new CharacterAnimation(PlayerCharacter.THOR);
        characterAnimations[i++] = new CharacterAnimation(PlayerCharacter.ARCHER);
    }

    private void loadBackground() {
        backgroundImg = LoadSave.GetSpriteAtlas(LoadSave.MENU_BACKGROUND);
        menuWidth = (int) (backgroundImg.getWidth() * Game.SCALE);
        menuHeight = (int) (backgroundImg.getHeight() * Game.SCALE);
        menuX = Game.GAME_WIDTH / 2 - menuWidth / 2;
        menuY = (int) (25 * Game.SCALE);
    }

    private void loadButtons() {
        playButton = new MenuButton(Game.GAME_WIDTH / 2, (int) (340 * Game.SCALE), 0, Gamestate.PLAYING);
    }

    @Override
    public void update() {
        playButton.update();
        for (CharacterAnimation ca : characterAnimations)
            ca.update();
    }

    @Override
    public void draw(Graphics g) {
        // 1. Vẽ nền
        g.drawImage(backgroundImgPink, 0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT, null);
        g.drawImage(backgroundImg, menuX, menuY, menuWidth, menuHeight, null);

        playButton.draw(g);

        // 2. Tính toán vị trí vẽ
        int yCenter = menuY + menuHeight / 2;
        int xCenter = menuX + menuWidth / 2;

        // Đẩy 2 nhân vật bên cạnh lùi vào trong 18% để không bị cắt mép bảng
        int xLeft = menuX + (int)(menuWidth * 0.18); 
        int xRight = menuX + menuWidth - (int)(menuWidth * 0.18); 

        // 3. VẼ NHÂN VẬT (Logic: Bên cạnh bé - Ở giữa to)

        // --- Vẽ con BÊN TRÁI (Bé: 0.7) ---
        drawChar(g, playerIndex - 1, xLeft, yCenter, 0.7f);

        // --- Vẽ con BÊN PHẢI (Bé: 0.7) ---
        drawChar(g, playerIndex + 1, xRight, yCenter, 0.7f);

        // --- Vẽ con Ở GIỮA (To đùng: 1.6 - Vẽ sau cùng để đè lên trên) ---
        drawChar(g, playerIndex, xCenter, yCenter, 0.9f);
    }

    // Hàm vẽ hỗ trợ có thêm tham số zoomFactor
    private void drawChar(Graphics g, int playerIndex, int x, int y, float zoomFactor) {
        if (playerIndex < 0)
            playerIndex = characterAnimations.length - 1;
        else if (playerIndex >= characterAnimations.length)
            playerIndex = 0;
        
        // Gọi hàm draw của animation với tham số zoom
        characterAnimations[playerIndex].draw(g, x, y, zoomFactor);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (isIn(e, playButton))
            playButton.setMousePressed(true);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (isIn(e, playButton)) {
            if (playButton.isMousePressed()) {
                game.getPlaying().setPlayerCharacter(characterAnimations[playerIndex].getPc());
                game.getAudioPlayer().setLevelSong(game.getPlaying().getLevelManager().getLevelIndex());
                playButton.applyGamestate();
            }
        }
        resetButtons();
    }

    private void resetButtons() {
        playButton.resetBools();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        playButton.setMouseOver(false);
        if (isIn(e, playButton))
            playButton.setMouseOver(true);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT)
            deltaIndex(1);
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT)
            deltaIndex(-1);
    }

    private void deltaIndex(int i) {
        playerIndex += i;
        if (playerIndex < 0)
            playerIndex = characterAnimations.length - 1;
        else if (playerIndex >= characterAnimations.length)
            playerIndex = 0;
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    // --- LỚP CON XỬ LÝ ANIMATION ---
    public class CharacterAnimation {
        private final PlayerCharacter pc;
        private int aniTick, aniIndex;
        private final BufferedImage[][] animations;
        private int baseScale; // Kích thước cơ bản

        public CharacterAnimation(PlayerCharacter pc) {
            this.pc = pc;
            this.baseScale = (int) (Game.SCALE + 6);
            animations = LoadSave.loadAnimations(pc);
        }

        // Hàm vẽ được cập nhật để nhận zoomFactor
        public void draw(Graphics g, int drawX, int drawY, float zoomFactor) {
            // Tính toán kích thước thực tế sau khi zoom
            int currentScale = (int) (baseScale * zoomFactor);

            g.drawImage(animations[pc.getRowIndex(IDLE)][aniIndex],
                    drawX - pc.spriteW * currentScale / 2,
                    drawY - pc.spriteH * currentScale / 2,
                    pc.spriteW * currentScale,
                    pc.spriteH * currentScale,
                    null);
        }

        public void update() {
            aniTick++;
            if (aniTick >= ANI_SPEED) {
                aniTick = 0;
                aniIndex++;
                if (aniIndex >= pc.getSpriteAmount(IDLE)) {
                    aniIndex = 0;
                }
            }
        }

        public PlayerCharacter getPc() {
            return pc;
        }
    }
}