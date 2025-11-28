package objects;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import main.Game;
import utilz.LoadSave;
import static utilz.HelpMethods.CanMoveHere;

public class Arrow extends Projectile {
	private BufferedImage arrowImg;
	private int damage = 20;

	// --- BIẾN ĐỂ TÍNH TẦM BẮN ---
	private float startX;
	private float range;

	public Arrow(float x, float y, int direction) {
		// Kích thước 60x20 (To như bạn muốn)
		super(x, y, (int) (60 * Game.SCALE), (int) (20 * Game.SCALE), (float) (1.5f * Game.SCALE * direction), 0);
		
		arrowImg = LoadSave.GetSpriteAtlas(LoadSave.ARROW_SPRITE);

		// --- THIẾT LẬP GIỚI HẠN TẦM BẮN ---
		this.startX = x; // Lưu vị trí bắt đầu
		this.range = Game.GAME_WIDTH / 2f; // Tầm bắn = 1/2 chiều rộng màn hình (nửa map)
	}

	@Override
	public void update(int[][] lvlData) {
		// Di chuyển
		x += velocityX;
		y += velocityY;
		
		// Cập nhật hitbox
		hitbox.x = x;
		hitbox.y = y;

		// 1. Kiểm tra va chạm tường (Nếu đụng tường thì mất)
		if (!CanMoveHere(x, y, width, height, lvlData)) {
			active = false;
		}

		// 2. --- KIỂM TRA TẦM BẮN (MỚI THÊM LẠI) ---
		// Nếu khoảng cách đã bay > range thì hủy mũi tên
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

	public int getDamage() {
		return damage;
	}
}