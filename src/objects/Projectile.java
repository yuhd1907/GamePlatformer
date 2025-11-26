package objects; // QUAN TRỌNG: PHẢI LÀ OBJECTS

import java.awt.Graphics;
import java.awt.geom.Rectangle2D; 

public abstract class Projectile {
	protected float x, y;
	protected int width, height;
	protected float velocityX, velocityY;
	protected boolean active = true;
	protected Rectangle2D.Float hitbox; // Dùng Float cho mượt

	public Projectile(float x, float y, int width, int height, float velocityX, float velocityY) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.velocityX = velocityX;
		this.velocityY = velocityY;
		this.hitbox = new Rectangle2D.Float(x, y, width, height);
	}

	public abstract void update(int[][] lvlData);
	public abstract void render(Graphics g, int xOffset);

	public boolean isActive() { return active; }
	public void setActive(boolean active) { this.active = active; }
	public Rectangle2D.Float getHitbox() { return hitbox; }
}