package madscience.content.abomination;

import madscience.MadConfig;
import madscience.MadMobs;
import madscience.MadScience;
import net.minecraft.block.Block;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;

import java.util.Random;

public class EntityAbomination extends EntityMob
{
    public static final String ABOMINATION_ATTACK = MadScience.ID + ":" + MadMobs.GMO_ABOMINATION_INTERNALNAME + ".attack";
    public static final String ABOMINATION_HISS = MadScience.ID + ":" + MadMobs.GMO_ABOMINATION_INTERNALNAME + ".hiss";
    public static final String ABOMINATION_GROWL = MadScience.ID + ":" + MadMobs.GMO_ABOMINATION_INTERNALNAME + ".growl";
    public static final String ABOMINATION_DEATH = MadScience.ID + ":" + MadMobs.GMO_ABOMINATION_INTERNALNAME + ".death";
    public static final String ABOMINATION_STEP = MadScience.ID + ":" + MadMobs.GMO_ABOMINATION_INTERNALNAME + ".step";
    public static final String ABOMINATION_PAIN = MadScience.ID + ":" + MadMobs.GMO_ABOMINATION_INTERNALNAME + ".pain";
    public static final String ABOMINATION_EGG = MadScience.ID + ":" + MadMobs.GMO_ABOMINATION_INTERNALNAME + ".egg";
    public static final String ABOMINATION_HATCH = MadScience.ID + ":" + MadMobs.GMO_ABOMINATION_INTERNALNAME + ".egghatch";
    public static final String ABOMINATION_EGGPOP = MadScience.ID + ":" + MadMobs.GMO_ABOMINATION_INTERNALNAME + ".eggpop";


    private Random rand = new Random();

    /**
     * Counter to delay the teleportation of an enderman towards the currently attacked target
     */
    private int teleportDelay;

    public EntityAbomination(World par1World)
    {
        super(par1World);

        // This is the hitbox size. Starts in the center and grows outwards.
        this.setSize(1.4F, 0.9F);
        this.stepHeight = 1.0F;

        // Defines if fire affects this mob.
        this.isImmuneToFire = true;

        // Swimming should ALWAYS be first. Otherwise if your mob falls in
        // water, but it's running away from you or something it'll drown.
        this.tasks.addTask(0, new EntityAISwimming(this));

        // Makes the mob try and attack the nearest player that it encounters.
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));

        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityLivingBase.class, 0, false, true, EntitySelectorAbomination.GENERIC));

        // Forces the player to take damage if the mob touches them.
        this.tasks.addTask(3, new EntityAIAttackOnCollide(this, 1.0D, false));
        this.tasks.addTask(5, new EntityAIWander(this, 0.8D));

        // This makes the mob watch the nearest player, within a range set by
        this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityLiving.class, 6.0F));

        // Finally, this makes it look around when it's not looking at a player
        // or wandering.
        this.tasks.addTask(8, new EntityAILookIdle(this));

    }

    @Override
    protected void applyEntityAttributes()
    {
        // Changes basic attributes about the mob to make it different than
        // others.
        super.applyEntityAttributes();

        // Set mob total amount of health (hearts).
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(42.0D);

        // Recommended default speed is used for the mob.
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.666666666666D);

        // How much damage does this mob do to other mobs.
        this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(10.0D);

        // How many blocks will the mob pursue it's target.
        this.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(42.0D);
    }

    /**
     * Basic mob attack. Default to touch of death in EntityCreature. Overridden by each mob to define their attack.
     */
    @Override
    protected void attackEntity(Entity par1Entity, float par2)
    {
        float f1 = this.getBrightness(1.0F);

        if (f1 > 0.5F && this.rand.nextInt(100) == 0)
        {
            this.entityToAttack = null;
        }
        else
        {
            super.attackEntity(par1Entity, par2);
        }
    }

    /**
     * Called when the entity is attacked.
     */
    @Override
    public boolean attackEntityFrom(DamageSource par1DamageSource, float par2)
    {
        if (this.isEntityInvulnerable())
        {
            return false;
        }
        else
        {
            if (par1DamageSource instanceof EntityDamageSourceIndirect)
            {
                for (int i = 0; i < 64; ++i)
                {
                    if (this.teleportRandomly())
                    {
                        return true;
                    }
                }

                return super.attackEntityFrom(par1DamageSource, par2);
            }
            else
            {
                if (this.worldObj.rand.nextBoolean())
                {
                    for (int i = 0; i < 64; ++i)
                    {
                        if (this.teleportRandomly())
                        {
                            return true;
                        }
                    }
                }

                return super.attackEntityFrom(par1DamageSource, par2);
            }
        }
    }

    @Override
    public boolean canAttackClass(Class par1Class)
    {
        return EntityAbomination.class != par1Class && EntityGhast.class != par1Class;
    }

    @Override
    protected void collideWithEntity(Entity par1Entity)
    {
        if (par1Entity instanceof IMob && !(par1Entity instanceof EntityAbomination) && this.getRNG().nextInt(20) == 0)
        {
            this.setAttackTarget((EntityLiving) par1Entity);
        }

        super.collideWithEntity(par1Entity);
    }

    // This is required regardless of if your animal can breed or not. Set to
    // null if it can't breed - I wont cover breeding here.
    public EntityAgeable createChild(EntityAgeable var1)
    {
        return null;
    }

    /**
     * Drop 0-2 items of this living's type. @param par1 - Whether this entity has recently been hit by a player. @param par2 - Level of Looting used to kill this mob.
     */
    @Override
    protected void dropFewItems(boolean par1, int par2)
    {
        super.dropFewItems(par1, par2);

        if (par1 && (this.rand.nextInt(3) == 0 || this.rand.nextInt(1 + par2) > 0))
        {
            this.dropItem(Items.spider_eye, 1);
            this.dropItem(Items.ender_pearl, 1);
        }
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();
        this.dataWatcher.addObject(16, new Byte((byte) 0));
        this.dataWatcher.addObject(17, new Byte((byte) 0));
        this.dataWatcher.addObject(18, new Byte((byte) 0));
    }

    /**
     * Finds the closest player within 16 blocks to attack, or null if this Entity isn't interested in attacking (Animals, Spiders at day, peaceful PigZombies).
     */
    @Override
    protected Entity findPlayerToAttack()
    {
        double d0 = 16.0D;
        if (this.rand.nextInt(100) == 0)
        {
            this.playSound(ABOMINATION_GROWL, 1.0F, 0.5F);
        }
        return this.worldObj.getClosestVulnerablePlayerToEntity(this, d0);
    }

    /**
     * Get this Entity's EnumCreatureAttribute
     */
    @Override
    public EnumCreatureAttribute getCreatureAttribute()
    {
        return EnumCreatureAttribute.ARTHROPOD;
    }

    // The sound made when it actually dies.
    @Override
    protected String getDeathSound()
    {
        return ABOMINATION_DEATH;
    }

    /**
     * Returns the item ID for the item the mob drops on death.
     */
    @Override
    protected Item getDropItem()
    {
        return Items.string;
    }

    // The sound made when it's attacked. Often it's the same as the normal say
    // sound, but sometimes different (such as in the ender dragon)
    @Override
    protected String getHurtSound()
    {
        return ABOMINATION_PAIN;
    }

    // The sound effect played when it's just living, like a cow mooing.
    @Override
    protected String getLivingSound()
    {
        return ABOMINATION_HISS;
    }

    @Override
    protected float getSoundVolume()
    {
        // Allows us to change how loud this particular entity is.
        return 0.4F;
    }

    // This is required. If it's false, none of the above takes effect.
    @Override
    public boolean isAIEnabled()
    {
        return true;
    }

    /**
     * Returns true if the WatchableObject (Byte) is 0x01 otherwise returns false. The WatchableObject is updated using setBesideClimableBlock.
     */
    public boolean isBesideClimbableBlock()
    {
        return (this.dataWatcher.getWatchableObjectByte(16) & 1) != 0;
    }

    /**
     * returns true if this entity is by a ladder, false otherwise
     */
    @Override
    public boolean isOnLadder()
    {
        return this.isBesideClimbableBlock();
    }

    @Override
    public boolean isPotionApplicable(PotionEffect par1PotionEffect)
    {
        return par1PotionEffect.getPotionID() == Potion.poison.id ? false : super.isPotionApplicable(par1PotionEffect);
    }

    public boolean isScreaming()
    {
        return this.dataWatcher.getWatchableObjectByte(18) > 0;
    }

    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons use this to react to sunlight and start to burn.
     */
    @Override
    public void onLivingUpdate()
    {
        int i;

        for (i = 0; i < 2; ++i)
        {
            this.worldObj.spawnParticle("portal", this.posX + (this.rand.nextDouble() - 0.5D) * this.width, this.posY + this.rand.nextDouble() * this.height - 0.25D, this.posZ + (this.rand.nextDouble() - 0.5D) * this.width,
                    (this.rand.nextDouble() - 0.5D) * 2.0D, -this.rand.nextDouble(), (this.rand.nextDouble() - 0.5D) * 2.0D);
        }

        this.isJumping = false;

        super.onLivingUpdate();
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void onUpdate()
    {
        super.onUpdate();

        if (!this.worldObj.isRemote)
        {
            this.setBesideClimbableBlock(this.isCollidedHorizontally);
        }
    }

    // The sound the mob plays when walking around.

    protected void playStepSound(int par1, int par2, int par3, int par4)
    {
        // First variable is volume and the second is pitch.
        this.worldObj.playSoundAtEntity(this, ABOMINATION_STEP, 1.0F, 1.0F);
    }

    /**
     * Updates the WatchableObject (Byte) created in entityInit(), setting it to 0x01 if par1 is true or 0x00 if it is false.
     */
    public void setBesideClimbableBlock(boolean par1)
    {
        byte b0 = this.dataWatcher.getWatchableObjectByte(16);

        if (par1)
        {
            b0 = (byte) (b0 | 1);
        }
        else
        {
            b0 &= -2;
        }

        this.dataWatcher.updateObject(16, Byte.valueOf(b0));
    }

    /**
     * Sets the Entity inside a web block.
     */
    @Override
    public void setInWeb()
    {
    }

    /**
     * Teleport the enderman to a random nearby position
     */
    protected boolean teleportRandomly()
    {
        if (!MadConfig.ABOMINATION_TELEPORTS)
        {
            return false;
        }

        double d0 = this.posX + (this.rand.nextDouble() - 0.5D) * 64.0D;
        double d1 = this.posY + (this.rand.nextInt(64) - 32);
        double d2 = this.posZ + (this.rand.nextDouble() - 0.5D) * 64.0D;
        return this.teleportTo(d0, d1, d2);
    }

    /**
     * Teleport the enderman
     */
    protected boolean teleportTo(double par1, double par3, double par5)
    {
        if (!MadConfig.ABOMINATION_TELEPORTS)
        {
            return false;
        }

        EnderTeleportEvent event = new EnderTeleportEvent(this, par1, par3, par5, 0);
        if (MinecraftForge.EVENT_BUS.post(event))
        {
            return false;
        }

        double d3 = this.posX;
        double d4 = this.posY;
        double d5 = this.posZ;
        this.posX = event.targetX;
        this.posY = event.targetY;
        this.posZ = event.targetZ;
        boolean flag = false;
        int i = MathHelper.floor_double(this.posX);
        int j = MathHelper.floor_double(this.posY);
        int k = MathHelper.floor_double(this.posZ);
        Block l;

        if (this.worldObj.blockExists(i, j, k))
        {
            boolean flag1 = false;

            while (!flag1 && j > 0)
            {
                l = this.worldObj.getBlock(i, j - 1, k);

                if (l != null && l.getMaterial().blocksMovement())
                {
                    flag1 = true;
                }
                else
                {
                    --this.posY;
                    --j;
                }
            }

            if (flag1)
            {
                this.setPosition(this.posX, this.posY, this.posZ);

                if (this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox).isEmpty() && !this.worldObj.isAnyLiquid(this.boundingBox))
                {
                    flag = true;
                }
            }
        }

        if (!flag)
        {
            this.setPosition(d3, d4, d5);
            return false;
        }
        else
        {
            short short1 = 128;

            for (int ii = 0; ii < short1; ++ii)
            {
                double d6 = ii / (short1 - 1.0D);
                float f = (this.rand.nextFloat() - 0.5F) * 0.2F;
                float f1 = (this.rand.nextFloat() - 0.5F) * 0.2F;
                float f2 = (this.rand.nextFloat() - 0.5F) * 0.2F;
                double d7 = d3 + (this.posX - d3) * d6 + (this.rand.nextDouble() - 0.5D) * this.width * 2.0D;
                double d8 = d4 + (this.posY - d4) * d6 + this.rand.nextDouble() * this.height;
                double d9 = d5 + (this.posZ - d5) * d6 + (this.rand.nextDouble() - 0.5D) * this.width * 2.0D;
                this.worldObj.spawnParticle("portal", d7, d8, d9, f, f1, f2);
            }

            this.worldObj.playSoundEffect(d3, d4, d5, "mob.endermen.portal", 1.0F, 1.0F);
            this.playSound("mob.endermen.portal", 1.0F, 1.0F);
            return true;
        }
    }

    /**
     * Teleport the enderman to another entity
     */
    protected boolean teleportToEntity(Entity par1Entity)
    {
        if (!MadConfig.ABOMINATION_TELEPORTS)
        {
            return false;
        }

        Vec3 vec3 = Vec3.createVectorHelper(this.posX - par1Entity.posX, this.boundingBox.minY + this.height / 2.0F - par1Entity.posY + par1Entity.getEyeHeight(), this.posZ - par1Entity.posZ);
        vec3 = vec3.normalize();
        double d0 = 16.0D;
        double d1 = this.posX + (this.rand.nextDouble() - 0.5D) * 8.0D - vec3.xCoord * d0;
        double d2 = this.posY + (this.rand.nextInt(16) - 8) - vec3.yCoord * d0;
        double d3 = this.posZ + (this.rand.nextDouble() - 0.5D) * 8.0D - vec3.zCoord * d0;
        return this.teleportTo(d1, d2, d3);
    }
}