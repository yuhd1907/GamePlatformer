package entities;

import static utilz.Constants.EnemyConstants.*;
import static utilz.Constants.Dialogue.*;
import static utilz.HelpMethods.CanMoveHere;
import static utilz.HelpMethods.IsFloor;
import static utilz.Constants.Directions.*;

import gamestates.Playing;

public class Bird extends Enemy {

    private boolean preRoll = true;
    private int tickSinceLastDmgToPlayer;
    private int tickAfterRollInIdle;
    private int rollDurationTick, rollDuration = 300;

    public Bird(float x, float y) {
        super(x, y, BIRD_WIDTH, BIRD_HEIGHT, BIRD);
        initHitbox(37, 28);
        initAttackBox(37, 28, 30);
    }

    public void update(int[][] lvlData, Playing playing) {
        updateBehavior(lvlData, playing);
        updateAnimationTick();
    }

    private void updateBehavior(int[][] lvlData, Playing playing) {
        if (firstUpdate)
            firstUpdateCheck(lvlData);

        if (inAir)
            inAirChecks(lvlData, playing);
        else {
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
                    break;
                case ATTACK:
                    if (aniIndex == 0)
                        attackChecked = false;
                    else if (aniIndex == 6) {
                        if (!attackChecked)
                            checkPlayerHit(attackBox, playing.getPlayer());
                        attackMove(lvlData, playing);
                    }
                    break;
                case HIT:
                    if (aniIndex <= GetSpriteAmount(enemyType, state) - 2)
                        pushBack(pushBackDir, lvlData, 2f);
                    updatePushBackDrawOffset();
                    tickAfterRollInIdle = 120;

                    break;
            }
        }
    }

    protected void attackMove(int[][] lvlData, Playing playing) {
        float xSpeed = 0;

        if (walkDir == LEFT)
            xSpeed = -walkSpeed;
        else
            xSpeed = walkSpeed;

        if (CanMoveHere(hitbox.x + xSpeed * 4, hitbox.y, hitbox.width, hitbox.height, lvlData))
            if (IsFloor(hitbox, xSpeed * 4, lvlData)) {
                hitbox.x += xSpeed * 4;
                return;
            }
        newState(IDLE);
        playing.addDialogue((int) hitbox.x, (int) hitbox.y, EXCLAMATION);
    }

    private void checkDmgToPlayer(Player player) {
        if (hitbox.intersects(player.getHitbox()))
            if (tickSinceLastDmgToPlayer >= 60) {
                tickSinceLastDmgToPlayer = 0;
                player.changeHealth(-GetEnemyDmg(enemyType), this);
            } else
                tickSinceLastDmgToPlayer++;
    }

    private void setWalkDir(Player player) {
        if (player.getHitbox().x > hitbox.x)
            walkDir = RIGHT;
        else
            walkDir = LEFT;

    }

    protected void move(int[][] lvlData, Playing playing) {
        float xSpeed = 0;

        if (walkDir == LEFT)
            xSpeed = -walkSpeed;
        else
            xSpeed = walkSpeed;

        if (state == ATTACK)
            xSpeed *= 2;

        if (CanMoveHere(hitbox.x + xSpeed, hitbox.y, hitbox.width, hitbox.height, lvlData))
            if (IsFloor(hitbox, xSpeed, lvlData)) {
                hitbox.x += xSpeed;
                return;
            }

        if (state == ATTACK) {
            rollOver(playing);
            rollDurationTick = 0;
        }

        changeWalkDir();

    }

    private void checkRollOver(Playing playing) {
        rollDurationTick++;
        if (rollDurationTick >= rollDuration) {
            rollOver(playing);
            rollDurationTick = 0;
        }
    }

    private void rollOver(Playing playing) {
        newState(IDLE);
        playing.addDialogue((int) hitbox.x, (int) hitbox.y, QUESTION);
    }

}