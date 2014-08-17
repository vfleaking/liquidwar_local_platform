package liquidwar.logic.situation;

/**
 * 玩家接口 
 * @author vfleaking
 *
 */
public interface Player {
	void init(Owner owner);
	Movement move(GameSituation situ);
	void destroy();
}
