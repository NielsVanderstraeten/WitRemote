package commands;

public class SetEnemy extends Command {

	public SetEnemy(String enemy){
		super(enemy, "Setting enemy to: "  + enemy, "wit.private.enemy");
		this.enemy = enemy;
	}
	
	private String enemy;
	
	public String getEnemy(){
		return enemy;
	}
}
