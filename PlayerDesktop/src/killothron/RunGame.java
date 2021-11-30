package killothron;

import static org.junit.Assert.fail;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.json.JSONObject;

import app.PlayerApp;
import app.loading.GameLoading;
import app.utils.GameUtil;
import main.Status;
import manager.Manager;
import manager.ai.AIDetails;
import manager.ai.AIUtil;

/**
 * Method used to run a game in the killothon.
 * 
 * @author Eric.Piette
 */
public class RunGame extends Thread
{
	/** The name of the game. */
	private final String gameName;
	
	/** The number of players. */
	private final int numPlayers;
	
	/** The graphical app used to show the game. */
	private final PlayerApp app;

	/**
	 * @param app        The app.
	 * @param name       The game name.
	 * @param numPlayers The number of players.
	 */
	public RunGame
	(
		final PlayerApp app, 
		final String name,
		final int numPlayers
	)
	{
		this.app = app;
		gameName = name;
		this.numPlayers = numPlayers;
	}

	@Override
	public void run()
	{
		final Manager manager = app.manager();
		
		try
		{
			EventQueue.invokeAndWait(() ->
			{
				GameLoading.loadGameFromName(app, gameName, new ArrayList<String>(), false);
				manager.ref().context().game().setMaxMoveLimit(numPlayers*200); // 200 moves per player.
				for(int pid = 1; pid <= numPlayers; pid++)
				{
					final String AIName = pid == 1 ? "UCT" : "Random";
					final JSONObject json = new JSONObject().put("AI", new JSONObject().put("algorithm", AIName));
					AIUtil.updateSelectedAI(app.manager(), json, pid, AIName);
							
					if (manager.aiSelected()[pid].ai() != null)
						manager.aiSelected()[pid].ai().closeAI();
							
					manager.aiSelected()[pid] = new AIDetails(manager, json, pid, "Random");
				}
				GameUtil.startGame(app);
				app.manager().settingsManager().setAgentsPaused(app.manager(), false);
				app.manager().ref().nextMove(app.manager(), false);
			});
		}
		catch (InvocationTargetException | InterruptedException e)
		{
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * Set the first player to random.
	 */
	public void setFirstPlayerToRandom()
	{
		final Manager manager = app.manager();
		final JSONObject json = new JSONObject().put("AI", new JSONObject().put("algorithm", "Random"));
		AIUtil.updateSelectedAI(app.manager(), json, 1, "Random");
				
		if (manager.aiSelected()[1].ai() != null)
			manager.aiSelected()[1].ai().closeAI();
				
		manager.aiSelected()[1] = new AIDetails(manager, json, 1, "Random");
		app.manager().settingsManager().setAgentsPaused(app.manager(), false);
		app.manager().ref().nextMove(app.manager(), false);
	}
	
	/**
	 * @return True if the game is over.
	 */
	public boolean isOver()
	{
		return app.manager().ref().context().trial().over();
	}
	
	/** 
	 * @return The game length of the current game. 
	 */
	public int gameLength()
	{
		return app.manager().ref().context().trial().numberRealMoves();
	}
	
	/**
	 * @return The status of the current game.
	 */
	public Status status()
	{
		return app.manager().ref().context().trial().status();
	}
	
	/**
	 * @return The status of the current game.
	 */
	public double[] ranking()
	{
		return app.manager().ref().context().trial().ranking();
	}
	
	/**
	 * @return The current mover.
	 */
	public int mover()
	{
		return app.manager().ref().context().state().mover();
	}
	
}
