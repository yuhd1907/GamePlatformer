package objects; 

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import main.Game;
import utilz.LoadSave;
import static utilz.HelpMethods.CanMoveHere;

public class Arrow extends Projectile {
    private BufferedImage arrowImg;
    private int damage = 20;

    // --- 1. THÊM BIẾN TÍNH KHOẢNG CÁCH ---
    private float startX;
    private float range;

    public Arrow(float x, float y, int direction) {
        super(x, y, (int) (60 * Game.SCALE), (int) (12 * Game.SCALE), (float) (1.5f * Game.SCALE * direction), 0);
        arrowImg = LoadSave.GetSpriteAtlas(LoadSave.ARROW_SPRITE); 

        // --- 2. THIẾT LẬP GIỚI HẠN ---
        this.startX = x; // Lưu vị trí bắt đầu bắn
        this.range = Game.GAME_WIDTH / 2f; 
    }

    @Override
    public void update(int[][] lvlData) {
        x += velocityX;
        y += velocityY;
        hitbox.x = x;
        hitbox.y = y;

        // Check va chạm tường (Logic cũ)
        if (!CanMoveHere(x, y, width, height, lvlData)) {
            active = false;
        }

        // --- 3. CHECK TẦM BẮN (MỚI) ---
        // Nếu khoảng cách đi được lớn hơn range -> Hủy mũi tên
        if (Math.abs(x - startX) >= range) {
            active = false;
        }
    }

    @Override
    public void render(Graphics g, int xOffset) {
        if (active) {
            if (velocityX > 0)
                g.drawImage(arrowImg, (int) (x - xOffset), (int) y, width, height, null);
            else
                g.drawImage(arrowImg, (int) (x - xOffset) + width, (int) y, -width, height, null);
        }
    }

    public int getDamage() { return damage; }
}