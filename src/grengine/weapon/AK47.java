package grengine.weapon;

public class AK47 extends BaseWeapon {

	
	public AK47()
	{
		super();
		setAmmoType("Rifle");
		setTracerTexture("resource/tracer.png");
		setShellTexture("resource/shell.png");
		setMagSize(24);
		setDamage(4);
		setBulletsPerShot(1);
		setFireRate(0.1f);
		setReloadTime(3);
		
		
	}
	
}
