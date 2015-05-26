package grengine.entity;

public class Weapon extends Entity {
	private String ammoType; // Extended clips need to be thought about.
	private String tracerTexture;
	private String shellTexture;
	private int magSize; // Number of bullets that can be fired before reloading.
	//private int maxMags; // Number of magazines a gun can have.  magSize * maxMags = number of bullets cl can have.  THIS SHOULD BE DEFINED ON PLAYER (number of mgas a PLAYER can have)
	private int curAmmo; // Current number of bullets in this gun's magazine.
	//private int curMags; // Magazines with the gun.  THIS SHOULD BE DEFINED ON PLAYER (INVENTORY?)
	private int damage; // The amount of damage a gun should deal.
	private int penetration = 0; // The amount of players bullets should go through.  0 by default.  May be implemented later.
	private int bulletsPerShot; // The number of bullets released each time you fire. ***
	private float fireRate; // The number of times you can shoot in one second.  Perhaps this should be a double/float? ***
	private float bpsDelay; // The delay in seconds between each bullet fired for bulletsPerShot.  Think battle rifle from Halo3  This should be a double/float. ***
	private float spread; // The radius of the base of a cone 1 unit away from he player.  Larger = greater spread/spray. ***
	private float reloadTime; // Reload time in seconds.  This should be a double/float. ***	
	public Weapon(String name)
	{
		super();
		this.name = name;
	}
	public String getAmmoType()
	{
		return this.ammoType;
	}
	public void setAmmoType(String ammoType)
	{
		this.ammoType = ammoType;
	}
	public int getDamage()
	{
		return this.damage;
	}
	public void setDamage(int damage)
	{
		this.damage = damage;
	}
	public String getTracerTexture()
	{
		return this.tracerTexture;
	}
	public void setTracerTexture(String tracerTexture)
	{
		this.tracerTexture = tracerTexture;
	}
	// Used to get the current number of bullets in the magazine.
	public int getAmmo()
	{
		return this.curAmmo;
	}
	// Used to set the amount of bullets in the magazine.
	public void setAmmo(int ammo)
	{
		this.curAmmo = ammo;
	}
	public int getPenetration()
	{
		return this.penetration;
	}
	public void setPenetration(int penetration)
	{
		this.penetration = penetration;
	}
	public String getShellTexture() {
		return shellTexture;
	}
	public void setShellTexture(String shellTexture) {
		this.shellTexture = shellTexture;
	}
	public int getMagSize() {
		return magSize;
	}
	public void setMagSize(int magSize) {
		this.magSize = magSize;
	}
	public int getBulletsPerShot() {
		return bulletsPerShot;
	}
	public void setBulletsPerShot(int bulletsPerShot) {
		this.bulletsPerShot = bulletsPerShot;
	}
	public float getFireRate() {
		return fireRate;
	}
	public void setFireRate(float fireRate) {
		this.fireRate = fireRate;
	}
	public float getBpsDelay() {
		return bpsDelay;
	}
	public void setBpsDelay(float bpsDelay) {
		this.bpsDelay = bpsDelay;
	}
	public float getSpread() {
		return spread;
	}
	public void setSpread(float spread) {
		this.spread = spread;
	}
	public float getReloadTime() {
		return reloadTime;
	}
	public void setReloadTime(float reloadTime) {
		this.reloadTime = reloadTime;
	}
}
