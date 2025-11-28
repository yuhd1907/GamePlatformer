package entities;

import static utilz.Constants.PlayerConstants.*;
import static utilz.HelpMethods.*;
import static utilz.Constants.*;
import static utilz.Constants.Directions.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList; 

import audio.AudioPlayer;
import gamestates.Playing;
import main.Game;
import utilz.LoadSave;

// Import Arrow từ package objects
import objects.Arrow; 

public class Player extends Entity {

    private BufferedImage[][] animations;
    private boolean moving = false, attacking = false;
    private boolean left, right, jump;
    private int[][] lvlData;

    private float jumpSpeed = -2.25f * Game.SCALE;
    private float fallSpeedAfterCollision = 0.5f * Game.SCALE;

    // UI Variables
    private BufferedImage statusBarImg;
    private int statusBarWidth = (int) (192 * Game.SCALE);
    private int statusBarHeight = (int) (58 * Game.SCALE);
    private int statusBarX = (int) (10 * Game.SCALE);
    private int statusBarY = (int) (10 * Game.SCALE);
    private int healthBarWidth = (int) (150 * Game.SCALE);
    private int healthBarHeight = (int) (4 * Game.SCALE);
    private int healthBarXStart = (int) (34 * Game.SCALE);
    private int healthBarYStart = (int) (14 * Game.SCALE);
    private int healthWidth = healthBarWidth;
    private int powerBarWidth = (int) (104 * Game.SCALE);
    private int powerBarHeight = (int) (2 * Game.SCALE);
    private int powerBarXStart = (int) (44 * Game.SCALE);
    private int powerBarYStart = (int) (34 * Game.SCALE);
    private int powerWidth = powerBarWidth;
    private int powerMaxValue = 200;
    private int powerValue = powerMaxValue;

    private int flipX = 0;
    private int flipW = 1;
    private boolean attackChecked;
    private Playing playing;
    private int tileY = 0;
    private boolean powerAttackActive;
    private int powerAttackTick;
    private int powerGrowSpeed = 15;
    private int powerGrowTick;

    private final PlayerCharacter playerCharacter;

    // --- DANH SÁCH MŨI TÊN ---
    private ArrayList<Arrow> arrows = new ArrayList<>();

    public Player(PlayerCharacter playerCharacter, Playing playing) {
        super(0, 0, (int) (playerCharacter.spriteW * Game.SCALE), (int) (playerCharacter.spriteH * Game.SCALE));
        this.playerCharacter = playerCharacter;
        this.playing = playing;
        this.state = IDLE;
        this.maxHealth = 100;
        this.currentHealth = maxHealth;
        this.walkSpeed = Game.SCALE * 1.0f;
        animations = LoadSave.loadAnimations(playerCharacter);
        statusBarImg = LoadSave.GetSpriteAtlas(LoadSave.STATUS_BAR);
        
        // Khởi tạo hitbox và attackbox dựa trên thông số nhân vật (đã có trong Enum PlayerCharacter)
        initHitbox(playerCharacter.hitboxW, playerCharacter.hitboxH);
        
        // Lấy kích thước AttackBox từ Enum (quan trọng cho THOR, ARCHER...)
        attackBox = new Rectangle2D.Float(x, y, playerCharacter.attackBoxW, playerCharacter.attackBoxH);
    }

    public void setSpawn(Point spawn) {
        this.x = spawn.x;
        this.y = spawn.y;
        hitbox.x = x;
        hitbox.y = y;
    }

    private void initAttackBox() {
        // AttackBox đã được khởi tạo trong Constructor, hàm này có thể để trống hoặc dùng để reset
    }

    public void update() {
        updateHealthBar();
        updatePowerBar();

        if (currentHealth <= 0) {
            if (state != DEAD) {
                state = DEAD;
                aniTick = 0;
                aniIndex = 0;
                playing.setPlayerDying(true);
                playing.getGame().getAudioPlayer().playEffect(AudioPlayer.DIE);
                if (!IsEntityOnFloor(hitbox, lvlData)) {
                    inAir = true;
                    airSpeed = 0;
                }
            } else if (aniIndex == playerCharacter.getSpriteAmount(DEAD) - 1 && aniTick >= ANI_SPEED - 1) {
                playing.setGameOver(true);
                playing.getGame().getAudioPlayer().stopSong();
                playing.getGame().getAudioPlayer().playEffect(AudioPlayer.GAMEOVER);
            } else {
                updateAnimationTick();
                if (inAir)
                    if (CanMoveHere(hitbox.x, hitbox.y + airSpeed, hitbox.width, hitbox.height, lvlData)) {
                        hitbox.y += airSpeed;
                        airSpeed += GRAVITY;
                    } else
                        inAir = false;
            }
            return;
        }

        updateAttackBox();

        if (state == HIT) {
            if (aniIndex <= playerCharacter.getSpriteAmount(state) - 3)
                pushBack(pushBackDir, lvlData, 1.25f);
            updatePushBackDrawOffset();
        } else
            updatePos();

        if (moving) {
            checkPotionTouched();
            checkSpikesTouched();
            checkInsideWater();
            tileY = (int) (hitbox.y / Game.TILES_SIZE);
            if (powerAttackActive) {
                powerAttackTick++;
                if (powerAttackTick >= 35) {
                    powerAttackTick = 0;
                    powerAttackActive = false;
                }
            }
        }

        if (attacking || powerAttackActive)
            checkAttack();

        updateAnimationTick();
        setAnimation();
        
        // Cập nhật mũi tên
        updateArrows(lvlData);
    }

    // --- CHECK ATTACK (LOGIC BẮN CUNG CHO ARCHER) ---
    private void checkAttack() {
        // 1. Xác định thời điểm gây sát thương (Impact Frame)
        int attackAnimIndex = 1; // Mặc định cho các con khác (nhanh)

        if (playerCharacter == PlayerCharacter.THOR) {
            // Với Thor, búa đập xuống đất thường ở frame thứ 3 hoặc 4
            // Bạn hãy thử thay số 3 này bằng 2 hoặc 4 nếu thấy chưa khớp animation
            attackAnimIndex = 4; 
        }

        // 2. Kiểm tra xem animation đã chạy đến frame gây sát thương chưa
        if (attackChecked || aniIndex != attackAnimIndex)
            return;
            
        attackChecked = true;

        // Nếu đang lướt (Chuột phải) -> Thoát ngay, không bắn tên/đánh
        if (powerAttackActive) {
            attackChecked = false;
            return; 
        }

        // Logic bắn tên: ÁP DỤNG CHO ARCHER
        if (playerCharacter == PlayerCharacter.ARCHER) {
            int dir = 1;
            if (flipW == -1) dir = -1;
            
            // Tạo mũi tên
            arrows.add(new Arrow(hitbox.x, hitbox.y + (5 * Game.SCALE), dir));
        } else {
            // Các nhân vật khác (THOR, WOMAN...) đánh cận chiến
            playing.checkEnemyHit(attackBox);
            playing.checkObjectHit(attackBox);
        }
        
        playing.getGame().getAudioPlayer().playAttackSound();
    }
    
    // --- QUẢN LÝ ARROW ---
    private void updateArrows(int[][] lvlData) {
        for (Arrow a : arrows) {
            if (a.isActive()) {
                a.update(lvlData);
            }
        }
    }

    private void drawArrows(Graphics g, int xLvlOffset) {
        for (Arrow a : arrows) {
            if (a.isActive()) {
                a.render(g, xLvlOffset);
            }
        }
    }
    
    public ArrayList<Arrow> getArrows() {
        return arrows;
    }
    // ---------------------

    private void checkInsideWater() {
        if (IsEntityInWater(hitbox, playing.getLevelManager().getCurrentLevel().getLevelData()))
            currentHealth = 0;
    }

    private void checkSpikesTouched() {
        playing.checkSpikesTouched(this);
    }

    private void checkPotionTouched() {
        playing.checkPotionTouched(hitbox);
    }

    // Cập nhật vị trí AttackBox (Dành cho Thor và các char cận chiến)
    private void updateAttackBox() {
        if (right || (powerAttackActive && flipW == 1)) {
            attackBox.x = hitbox.x + hitbox.width + (int) (Game.SCALE * 10);
        } else if (left || (powerAttackActive && flipW == -1)) {
            attackBox.x = hitbox.x - hitbox.width - (int) (Game.SCALE * 10) - attackBox.width;
        }
        
        attackBox.y = hitbox.y + (Game.SCALE * 10);
    }

    private void updateHealthBar() {
        healthWidth = (int) ((currentHealth / (float) maxHealth) * healthBarWidth);
    }

    private void updatePowerBar() {
        powerWidth = (int) ((powerValue / (float) powerMaxValue) * powerBarWidth);
        powerGrowTick++;
        if (powerGrowTick >= powerGrowSpeed) {
            powerGrowTick = 0;
            changePower(1);
        }
    }

    public void render(Graphics g, int lvlOffset) {
        g.drawImage(animations[playerCharacter.getRowIndex(state)][aniIndex], (int) (hitbox.x - playerCharacter.xDrawOffset) - lvlOffset + flipX, (int) (hitbox.y - playerCharacter.yDrawOffset + (int) (pushDrawOffset)), width * flipW, height, null);
        drawHitbox(g, lvlOffset);
        
        // Vẽ mũi tên
        drawArrows(g, lvlOffset);
        
        // Vẽ AttackBox (Bật lên nếu cần debug)
        // drawAttackBox(g, lvlOffset); 
        
        drawUI(g);
    }

    private void drawUI(Graphics g) {
        g.drawImage(statusBarImg, statusBarX, statusBarY, statusBarWidth, statusBarHeight, null);
        g.setColor(Color.red);
        g.fillRect(healthBarXStart + statusBarX, healthBarYStart + statusBarY, healthWidth, healthBarHeight);
        g.setColor(Color.yellow);
        g.fillRect(powerBarXStart + statusBarX, powerBarYStart + statusBarY, powerWidth, powerBarHeight);
    }

    private void updateAnimationTick() {
        aniTick++;
        
        // --- CHỈNH TỐC ĐỘ RIÊNG CHO THOR ---
        // Mặc định ANI_SPEED có thể là 25 (chậm).
        // Nếu là THOR và đang ATTACK, ta giảm speed xuống còn 15 (càng nhỏ càng nhanh)
        int speed = ANI_SPEED;
        if (playerCharacter == PlayerCharacter.THOR && state == ATTACK) {
            speed = 15; // Tăng tốc độ đánh (số càng nhỏ càng nhanh)
        }
        // ------------------------------------

        if (aniTick >= speed) { // Thay ANI_SPEED bằng biến speed vừa tạo
            aniTick = 0;
            aniIndex++;
            if (aniIndex >= playerCharacter.getSpriteAmount(state)) {
                aniIndex = 0;
                attacking = false;
                attackChecked = false;
                if (state == HIT) {
                    newState(IDLE);
                    airSpeed = 0f;
                    if (!IsFloor(hitbox, 0, lvlData))
                        inAir = true;
                }
            }
        }
    }

    private void setAnimation() {
        int startAni = state;
        if (state == HIT) return;
        if (moving) state = RUNNING;
        else state = IDLE;
        if (inAir) {
            if (airSpeed < 0) state = JUMP;
            else state = FALLING;
        }
        if (powerAttackActive) {
            state = ATTACK;
            aniIndex = 1;
            aniTick = 0;
            return;
        }
        if (attacking) {
            state = ATTACK;
            if (startAni != ATTACK) {
                aniIndex = 1;
                aniTick = 0;
                return;
            }
        }
        if (startAni != state) resetAniTick();
    }

    private void resetAniTick() {
        aniTick = 0;
        aniIndex = 0;
    }

    private void updatePos() {
        moving = false;
        if (jump) jump();
        if (!inAir) if (!powerAttackActive) 
            if ((!left && !right) || (right && left)) 
                if (!attacking) return; // Fix lỗi đứng yên không bắn được

        float xSpeed = 0;
        if (left && !right) {
            xSpeed -= walkSpeed;
            flipX = width;
            flipW = -1;
        }
        if (right && !left) {
            xSpeed += walkSpeed;
            flipX = 0;
            flipW = 1;
        }
        if (powerAttackActive) {
            if ((!left && !right) || (left && right)) {
                if (flipW == -1) xSpeed = -walkSpeed;
                else xSpeed = walkSpeed;
            }
            xSpeed *= 3;
        }
        if (!inAir) if (!IsEntityOnFloor(hitbox, lvlData)) inAir = true;
        if (inAir && !powerAttackActive) {
            if (CanMoveHere(hitbox.x, hitbox.y + airSpeed, hitbox.width, hitbox.height, lvlData)) {
                hitbox.y += airSpeed;
                airSpeed += GRAVITY;
                updateXPos(xSpeed);
            } else {
                hitbox.y = GetEntityYPosUnderRoofOrAboveFloor(hitbox, airSpeed);
                if (airSpeed > 0) resetInAir();
                else airSpeed = fallSpeedAfterCollision;
                updateXPos(xSpeed);
            }
        } else updateXPos(xSpeed);
        moving = true;
    }

    private void jump() {
        if (inAir) return;
        playing.getGame().getAudioPlayer().playEffect(AudioPlayer.JUMP);
        inAir = true;
        airSpeed = jumpSpeed;
    }

    private void resetInAir() {
        inAir = false;
        airSpeed = 0;
    }

    private void updateXPos(float xSpeed) {
        if (CanMoveHere(hitbox.x + xSpeed, hitbox.y, hitbox.width, hitbox.height, lvlData))
            hitbox.x += xSpeed;
        else {
            hitbox.x = GetEntityXPosNextToWall(hitbox, xSpeed);
            if (powerAttackActive) {
                powerAttackActive = false;
                powerAttackTick = 0;
            }
        }
    }

    public void changeHealth(int value) {
		// value < 0 nghĩa là đang bị trừ máu (nhận sát thương)
		if (value < 0) {
			
			// --- LOGIC MỚI: NỘI TẠI GIẢM SÁT THƯƠNG CHO WOMAN ---
			if (playerCharacter == PlayerCharacter.WOMAN) {
				// Chia đôi sát thương
				// Ví dụ: Quái đánh -20 => Chỉ còn -10
				// Ví dụ: Pháo bắn -25 => Chỉ còn -12
				value = value / 2; 
			}
			// ----------------------------------------------------

			if (state == HIT)
				return;
			else
				newState(HIT);
		}

		currentHealth += value;
		currentHealth = Math.max(Math.min(currentHealth, maxHealth), 0);
	}

    public void changeHealth(int value, Enemy e) {
        if (state == HIT) return;
        changeHealth(value);
        pushBackOffsetDir = UP;
        pushDrawOffset = 0;
        if (e.getHitbox().x < hitbox.x) pushBackDir = RIGHT;
        else pushBackDir = LEFT;
    }

    public void kill() { currentHealth = 0; }
    
    public void changePower(int value) {
        powerValue += value;
        powerValue = Math.max(Math.min(powerValue, powerMaxValue), 0);
    }

    public void loadLvlData(int[][] lvlData) {
        this.lvlData = lvlData;
        if (!IsEntityOnFloor(hitbox, lvlData)) inAir = true;
    }
    
    public void resetDirBooleans() { left = false; right = false; }
    public void setAttacking(boolean attacking) { this.attacking = attacking; }
    public boolean isLeft() { return left; }
    public void setLeft(boolean left) { this.left = left; }
    public boolean isRight() { return right; }
    public void setRight(boolean right) { this.right = right; }
    public void setJump(boolean jump) { this.jump = jump; }
    
    public void resetAll() {
        resetDirBooleans();
        inAir = false;
        attacking = false;
        moving = false;
        airSpeed = 0f;
        state = IDLE;
        currentHealth = maxHealth;
        powerAttackActive = false;
        powerAttackTick = 0;
        powerValue = powerMaxValue;
        
        arrows.clear(); 

        hitbox.x = x;
        hitbox.y = y;
        
        if (!IsEntityOnFloor(hitbox, lvlData)) inAir = true;
    }
    
    public int getTileY() { return tileY; }
    
    public void powerAttack() {
        if (powerAttackActive) return;
        if (powerValue >= 60) {
            powerAttackActive = true;
            changePower(-60);
        }
    }
    
    // Đổi tên hàm thành isArcher cho đúng nghĩa
    public boolean isArcher() {
        return this.playerCharacter == PlayerCharacter.ARCHER;
    }
}