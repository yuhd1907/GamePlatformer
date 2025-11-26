package objects; // QUAN TRỌNG: PHẢI LÀ OBJECTS

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import main.Game;
import utilz.LoadSave;
import static utilz.HelpMethods.CanMoveHere;

public class Arrow extends Projectile {
	private BufferedImage arrowImg;
	private int damage = 20;

	public Arrow(float x, float y, int direction) {
		super(x, y, (int) (60 * Game.SCALE), (int) (12 * Game.SCALE), (float) (1.5f * Game.SCALE * direction), 0);
		// Đảm bảo tên biến ảnh trong LoadSave đúng là ARROW hoặc ARROW_SPRITE
		arrowImg = LoadSave.GetSpriteAtlas(LoadSave.ARROW_SPRITE); 
	}

	@Override
	public void update(int[][] lvlData) {
		x += velocityX;
		y += velocityY;
		hitbox.x = x;
		hitbox.y = y;

		if (!CanMoveHere(x, y, width, height, lvlData)) {
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