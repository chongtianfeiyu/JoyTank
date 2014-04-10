package com.joytank.game;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Spatial;
import com.joytank.net.Consts;
import com.joytank.net.JoinRequest;
import com.joytank.net.JoinResponse;
import com.joytank.net.PingMsg;
import com.joytank.net.PlayerMotionMsg;

/**
 * 
 */
public class DefaultClientApplication extends AbstractApplication {

  private static final Logger logger = Logger.getLogger(DefaultClientApplication.class);

  protected final SocketAddress serverAddress;
  
  protected int clientId;
  protected int pingValue;
  protected CameraNode camNode;
  
  protected volatile boolean isPingServer = false;

  /**
   * 
   * 
   * @param serverHost @Nonnull server host name
   * @param serverPort server port number
   */
  public DefaultClientApplication(String serverHost, int serverPort) {
    super();
    Preconditions.checkState(!StringUtils.isBlank(serverHost), "serverHost is unexpectedly blank or null.");
    this.serverAddress = new InetSocketAddress(serverHost, serverPort);
  }

  @Override
  protected void initAll() {
  	setupHud();
  	setUpLights();
		setupCamera();
		setupInput();
		sendJoinRequest();
  }

  @Override
  protected void handleMessage(Object msg) {
    if (msg instanceof PingMsg) {
      handlePingMsg((PingMsg) msg);
    }
    if (msg instanceof JoinRequest) {
    	handleJoinResponse((JoinResponse) msg);
    }
    if (msg instanceof GameState) {
    	handleGameState((GameState) msg);
    }
    if (msg instanceof PlayerMotionMsg) {
      handlePlayerMotionMsg((PlayerMotionMsg) msg);
    }
  }
  
  @Override
  public void simpleUpdate(float tpf) {
    super.simpleUpdate(tpf);
    updatePing();
  }

  private void handlePingMsg(PingMsg msg) {
    long dTime = System.nanoTime() - msg.getTimestamp();
    pingValue = (int) (dTime / 1000000);
  }
  
  private void handleJoinResponse(JoinResponse msg) {
  	if (msg.getCliendId() != Consts.INVALID_CLIENT_ID) {
  		if (clientId == Consts.INVALID_CLIENT_ID) {
  			clientId = msg.getCliendId();
  			logger.info("Server accepted join request, assigned ID: " + clientId);
  		} else {
  			logger.info("A new client joined.");
  		}
  		handleGameState(msg.getGameState());
  	} else {
  		logger.info("Server declined join request, now exit.");
  		System.exit(1);
  	}
  }
  
  private void handleGameState(GameState msg) {
  	for (Entry<Integer, PlayerState> entry : msg.getPlayerStateMap().entrySet()) {
  		int id = entry.getKey();
  		PlayerState playerState = entry.getValue();
  		Player player = playerMap.get(id);
  		if (player != null) {
  			Vector3f serverLocation = playerState.getLocation();
  			CharacterControl characterControl = player.getControl(CharacterControl.class);
  			Vector3f clientLocation = characterControl.getPhysicsLocation();
  			
  			// Correct the client player location
  			if (serverLocation.distance(clientLocation) > 10f) {
  				characterControl.setPhysicsLocation(serverLocation);
  			}
  		} else {
  			player = Player.loadWithCapsuleCollisionShape("assets/models/Oto.zip", "main.scene", assetManager);
  			CharacterControl characterControl = player.getControl(CharacterControl.class);
  			characterControl.setPhysicsLocation(playerState.getLocation());
  			characterControl.setWalkDirection(playerState.getWalkDirection());
  			characterControl.setViewDirection(playerState.getWalkDirection());
  			addToGame(player);
  			bulletAppState.getPhysicsSpace().add(characterControl);
  			playerMap.putIfAbsent(id, player);
  		}
  	}
  }
  
  private void handlePlayerMotionMsg(PlayerMotionMsg msg) {
    Player player = playerMap.get(msg.getClientId());
    if (player == null) {
      logger.info("Player does not exist, ID: " + msg.getClientId());
      return;
    }
    
    // TODO do motion logic
    player.move(msg.getDst());
  }
  
  private void sendJoinRequest() {
    udpComponent.sendMsg(new JoinRequest(localAddress), serverAddress);
  }
  
  /**
   * 
   */
	private void setupHud() {
		//setup Ping value reading
		BitmapText pingText = new BitmapText(guiFont, false);
		pingText.setName(String.valueOf("ping"));
		pingText.setSize(guiFont.getCharSet().getRenderedSize());
		pingText.setColor(ColorRGBA.Black);
		pingText.setLocalTranslation(0, settings.getHeight(), 0);
		guiNode.attachChild(pingText);
	}
  
	/**
	 * 
	 */
	private void setUpLights() {
		viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));

		AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.White.mult(1.3f));
		rootNode.addLight(al);

		DirectionalLight dl = new DirectionalLight();
		dl.setColor(ColorRGBA.White);
		dl.setDirection(new Vector3f(2.8f, -2.8f, -2.8f).normalizeLocal());
		rootNode.addLight(dl);
	}
	
	/**
	 * 
	 */
	private void setupCamera() {
		int camDist = 80;
		camNode = new CameraNode("Camera Node", cam);
		camNode.setLocalTranslation(0, 0 + camDist, 0 - camDist);
		camNode.lookAt(Vector3f.ZERO, new Vector3f(0, 0.707f, 0.707f));
		rootNode.attachChild(camNode);
	}
	
	/**
	 * 
	 */
	private void setupInput() {
	  flyCam.setEnabled(false);
		inputManager.addMapping("move", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
		inputManager.addListener(new MyActionListener(), "move");
	}

	/**
	 * 
	 */
	class MyActionListener implements ActionListener {
		@Override
		public void onAction(String arg0, boolean arg1, float arg2) {
			if (arg0.equals("move") && arg1) {
				CollisionResults results = cursorRayIntTest(stage.getSpatial());
				if (results.size() > 0) {
					CollisionResult cr = results.getClosestCollision();
					udpComponent.sendMsg(new PlayerMotionMsg(clientId, cr.getContactPoint()), serverAddress);
				}
			}
		}
	}
	
	/**
	 * 
	 * @param spatial
	 * @return
	 */
	private CollisionResults cursorRayIntTest(Spatial spatial) {
		CollisionResults results = new CollisionResults();
		Vector2f click2d = inputManager.getCursorPosition();
		Vector3f click3d = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 0f).clone();
		Vector3f dir = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), cam.getFrustumNear())
		    .subtractLocal(click3d).normalizeLocal();
		Ray ray = new Ray(click3d, dir);
		spatial.collideWith(ray, results);
		return results;
	}
	
	private void updatePing() {
		BitmapText pingTxt = (BitmapText) guiNode.getChild("ping");
		pingTxt.setText("Ping: " + pingValue);
	}

  /**
   * Stop pinging the server
   */
  public void stopPingingServer() {
    isPingServer = false;
  }

  /**
   * Start to ping the server on a timely basis
   */
  public void startPingingServer() {
    if (isPingServer) {
      logger.info("Pinging daemon thread is already running.");
      return;
    }

    ExecutorService exec = Executors.newFixedThreadPool(1, new ThreadFactory() {
      @Override
      public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
      }
    });
    exec.execute(new PingTask());
  }

  /**
   * 
   */
  private class PingTask implements Runnable {
    @Override
    public void run() {
      try {
        isPingServer = true;
        while (isPingServer) {
          PingMsg pingMsg = new PingMsg(clientId, System.nanoTime());
          udpComponent.sendMsg(pingMsg, serverAddress);
          Thread.sleep(Consts.PING_INTERVAL_MILLISEC);
        }
      } catch (InterruptedException e) {
        logger.warn("InterruptedException", e);
      }
    }

  }
}
