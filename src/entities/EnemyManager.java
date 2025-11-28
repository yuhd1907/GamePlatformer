package entities;

import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import gamestates.Playing;
import levels.Level;
import utilz.LoadSave;
import static utilz.Constants.EnemyConstants.*;

// --- QUAN TRỌNG: PHẢI CÓ DÒNG NÀY VÌ ARROW Ở FOLDER OBJECTS ---
import objects.Arrow;
// -------------------------------------------------------------

public class EnemyManager {

    private Playing playing;
    private BufferedImage[][] goblinArr, pinkstarArr, mushroomArr;
    private Level currentLevel;

    public EnemyManager(Playing playing) {
        this.playing = playing;
        loadEnemyImgs();
    }

    public void loadEnemies(Level level) {
        this.currentLevel = level;
    }

    public void update(int[][] lvlData) {
        boolean isAnyActive = false;
        for (Goblin c : currentLevel.getGobs())
            if (c.isActive()) {
                c.update(lvlData, playing);
                isAnyActive = true;
            }

        for (Bird p : currentLevel.getBirds())
            if (p.isActive()) {
                p.update(lvlData, playing);
                isAnyActive = true;
            }

        for (Mushroom s : currentLevel.getMushs())
            if (s.isActive()) {
                s.update(lvlData, playing);
                isAnyActive = true;
            }

        if (!isAnyActive)
            playing.setLevelCompleted(true);
    }

    public void draw(Graphics g, int xLvlOffset) {
        drawGobs(g, xLvlOffset);
        drawBirds(g, xLvlOffset);
        drawMushs(g, xLvlOffset);
    }

    private void drawMushs(Graphics g, int xLvlOffset) {
        for (Mushroom s : currentLevel.getMushs())
            if (s.isActive()) {
                g.drawImage(mushroomArr[s.getState()][s.getAniIndex()],
                        (int) s.getHitbox().x - xLvlOffset - MUSHROOM_DRAWOFFSET_X + s.flipX(),
                        (int) s.getHitbox().y - MUSHROOM_DRAWOFFSET_Y + (int) s.getPushDrawOffset(),
                        MUSHROOM_WIDTH * s.flipW(), MUSHROOM_HEIGHT, null);
            }
    }

    private void drawBirds(Graphics g, int xLvlOffset) {
        for (Bird p : currentLevel.getBirds())
            if (p.isActive()) {
                g.drawImage(pinkstarArr[p.getState()][p.getAniIndex()],
                        (int) p.getHitbox().x - xLvlOffset - BIRD_DRAWOFFSET_X + p.flipX(),
                        (int) p.getHitbox().y - BIRD_DRAWOFFSET_Y + (int) p.getPushDrawOffset(),
                        BIRD_WIDTH * p.flipW(), BIRD_HEIGHT, null);
            }
    }

    private void drawGobs(Graphics g, int xLvlOffset) {
        for (Goblin c : currentLevel.getGobs())
            if (c.isActive()) {
                g.drawImage(goblinArr[c.getState()][c.getAniIndex()],
                        (int) c.getHitbox().x - xLvlOffset - GOBLIN_DRAWOFFSET_X + c.flipX(),
                        (int) c.getHitbox().y - GOBLIN_DRAWOFFSET_Y + (int) c.getPushDrawOffset(),
                        GOBLIN_WIDTH * c.flipW(), GOBLIN_HEIGHT, null);
            }
    }

    // Kiểm tra va chạm khi người chơi chém (cận chiến)
    public void checkEnemyHit(Rectangle2D.Float attackBox) {
        for (Goblin c : currentLevel.getGobs())
            if (c.isActive())
                if (c.getState() != DEAD && c.getState() != HIT)
                    if (attackBox.intersects(c.getHitbox())) {
                        c.hurt(20);
                        return;
                    }

        for (Bird p : currentLevel.getBirds())
            if (p.isActive()) {
                if (p.getState() == ATTACK && p.getAniIndex() >= 3)
                    return;
                else {
                    if (p.getState() != DEAD && p.getState() != HIT)
                        if (attackBox.intersects(p.getHitbox())) {
                            p.hurt(20);
                            return;
                        }
                }
            }

        for (Mushroom s : currentLevel.getMushs())
            if (s.isActive()) {
                if (s.getState() != DEAD && s.getState() != HIT)
                    if (attackBox.intersects(s.getHitbox())) {
                        s.hurt(20);
                        return;
                    }
            }
    }

    // --- HÀM KIỂM TRA MŨI TÊN (ĐÃ SỬA ĐỂ NHẬN LIST ARROW) ---
    public void checkEnemyHit(ArrayList<Arrow> arrows) {
        for (Arrow a : arrows) {
            // Nếu mũi tên này đã trúng cái gì đó hoặc không hoạt động thì bỏ qua
            if (!a.isActive()) continue;

            // 1. Check Cua
            for (Goblin c : currentLevel.getGobs())
                if (c.isActive() && c.getState() != DEAD && c.getState() != HIT)
                    if (a.getHitbox().intersects(c.getHitbox())) {
                        c.hurt(a.getDamage());
                        a.setActive(false); // Mũi tên biến mất
                    }

            if (!a.isActive()) continue; // Nếu trúng Cua rồi thì không check con khác nữa

            // 2. Check Sao biển
            for (Bird p : currentLevel.getBirds())
                if (p.isActive()) {
                    if (p.getState() == ATTACK && p.getAniIndex() >= 3)
                        continue; // Đang lăn thì né được
                    if (p.getState() != DEAD && p.getState() != HIT)
                        if (a.getHitbox().intersects(p.getHitbox())) {
                            p.hurt(a.getDamage());
                            a.setActive(false);
                        }
                }

            if (!a.isActive()) continue;

            // 3. Check Cá mập
            for (Mushroom s : currentLevel.getMushs())
                if (s.isActive())
                    if (s.getState() != DEAD && s.getState() != HIT)
                        if (a.getHitbox().intersects(s.getHitbox())) {
                            s.hurt(a.getDamage());
                            a.setActive(false);
                        }
        }
    }

    private void loadEnemyImgs() {
        goblinArr = getImgArr(LoadSave.GetSpriteAtlas(LoadSave.GOBLIN_SPRITE), 8, 5, GOBLIN_WIDTH_DEFAULT, GOBLIN_HEIGHT_DEFAULT);
        pinkstarArr = getImgArr(LoadSave.GetSpriteAtlas(LoadSave.BIRD_SPRITE), 8, 5, BIRD_WIDTH_DEFAULT, BIRD_HEIGHT_DEFAULT);
        mushroomArr = getImgArr(LoadSave.GetSpriteAtlas(LoadSave.MUSHROOM_SPRITE), 8, 5, MUSHROOM_WIDTH_DEFAULT, MUSHROOM_HEIGHT_DEFAULT);
    }

    private BufferedImage[][] getImgArr(BufferedImage atlas, int xSize, int ySize, int spriteW, int spriteH) {
        BufferedImage[][] tempArr = new BufferedImage[ySize][xSize];
        for (int j = 0; j < tempArr.length; j++)
            for (int i = 0; i < tempArr[j].length; i++)
                tempArr[j][i] = atlas.getSubimage(i * spriteW, j * spriteH, spriteW, spriteH);
        return tempArr;
    }

    public void resetAllEnemies() {
        for (Goblin c : currentLevel.getGobs())
            c.resetEnemy();
        for (Bird p : currentLevel.getBirds())
            p.resetEnemy();
        for (Mushroom s : currentLevel.getMushs())
            s.resetEnemy();
    }
}