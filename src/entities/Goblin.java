package entities;

import static utilz.Constants.EnemyConstants.*;
import static utilz.HelpMethods.IsFloor;
import static utilz.Constants.Dialogue.*;

import gamestates.Playing;

public class Goblin extends Enemy {

    public Goblin(float x, float y) {
        super(x, y, GOBLIN_WIDTH, GOBLIN_HEIGHT, GOBLIN);
        initHitbox(24, 27);
        initAttackBox(88, 27, 30);
    }

    public void update(int[][] lvlData, Playing playing) {
        updateBehavior(lvlData, playing);
        updateAnimationTick();
        updateAttackBox();
    }

    private void updateBehavior(int[][] lvlData, Playing playing) {
        if (firstUpdate)
            firstUpdateCheck(lvlData);

        if (inAir) {
            inAirChecks(lvlData, playing);
        } else {
            switch (state) {
                case IDLE:
                    if (IsFloor(hitbox, lvlData))
                        newState(RUNNING);
                    else
                        inAir = true;
                    break;
                case RUNNING:
                    if (canSeePlayer(lvlData, playing.getPlayer())) {
                        turnTowardsPlayer(playing.getPlayer());
                        if (isPlayerCloseForAttack(playing.getPlayer()))
                            newState(ATTACK);
                    }
                    move(lvlData);

                    if (inAir)
                        playing.addDialogue((int) hitbox.x, (int) hitbox.y, EXCLAMATION);

                    break;
                case ATTACK:
                    // Reset biến kiểm tra khi bắt đầu đòn đánh (frame 0)
                    if (aniIndex == 0)
                        attackChecked = false;

                    // --- SỬA THỜI ĐIỂM GÂY SÁT THƯƠNG TẠI ĐÂY ---
                    // aniIndex == 3: Gây sát thương ở frame thứ 4 (đòn đánh đã vung xuống)
                    // Nếu muốn chậm hơn nữa, bạn có thể sửa số 3 thành 4 (nếu animation có đủ frame)
                    if (aniIndex == 6 && !attackChecked)
                        checkPlayerHit(attackBox, playing.getPlayer());

                    break;
                case HIT:
                    if (aniIndex <= GetSpriteAmount(enemyType, state) - 2)
                        pushBack(pushBackDir, lvlData, 2f);
                    updatePushBackDrawOffset();
                    break;
            }
        }
    }

}