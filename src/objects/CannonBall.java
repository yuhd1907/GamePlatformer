package objects;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import main.Game;
import utilz.LoadSave;
import static utilz.HelpMethods.CanMoveHere;
import static utilz.Constants.Projectiles.*; 

public class CannonBall extends Projectile {
	private BufferedImage img;

	public CannonBall(int x, int y, int dir) {
		// Kích thước đạn pháo: 15x15, Tốc độ 0.75f
		// --- SỬA Ở ĐÂY: THÊM SỐ 0 VÀO CUỐI CÙNG ---
		super(x, y, (int)(15 * Game.SCALE), (int)(15 * Game.SCALE), (float)(0.75f * Game.SCALE * dir), 0);
		
		this.img = LoadSave.GetSpriteAtlas(LoadSave.CANNON_BALL);
	}

	@Override
	public void update(int[][] lvlData) {
		x += velocityX;
		y += velocityY; // Cập nhật cả Y (dù là 0)
		
		hitbox.x = x;
		hitbox.y = y;

		if (!CanMoveHere(x, y, width, height, lvlData))
			active = false;
	}

	@Override
	public void render(Graphics g, int xOffset) {
		if (active)
			g.drawImage(img, (int)(x - xOffset), (int)y, width, height, null);
	}
}