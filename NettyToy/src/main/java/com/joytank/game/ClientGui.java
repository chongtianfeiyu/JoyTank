package com.joytank.game;

import org.apache.log4j.Logger;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl.ControlDirection;

/**
 * Main GUI of the client side, this needs to replace the simple
 * {@link ClientUi}
 * 
 * @author lizhaoliu
 */
public class ClientGui extends SimpleApplication {

	private static final Logger LOGGER = Logger.getLogger(ClientGui.class);

	private Spatial sceneModel;
	private Node actor;

	private RigidBodyControl sceneBodyControl;
	private RigidBodyControl actorControl;
	private CharacterControl charControl;

	private AnimChannel animChannel;
	private AnimControl animControl;

	private BulletAppState bulletAppState;

	private CameraNode camNode;

	private Vector3f direction = new Vector3f();

	@Override
	public void simpleInitApp() {
		bulletAppState = new BulletAppState();
		stateManager.attach(bulletAppState);

		assetManager.registerLocator("assets/models/Oto.zip", ZipLocator.class);
		actor = (Node) assetManager.loadModel("Oto.mesh.xml");
		actor.move(0, 4.5f, 0);
		actor.setLocalScale(0.75f);
		rootNode.attachChild(actor);

		animControl = actor.getControl(AnimControl.class);
		animControl.addListener(new AnimEvtListenerImpl());
		animChannel = animControl.createChannel();
		animChannel.setAnim("stand");

		assetManager.registerLocator("assets/models/town.zip", ZipLocator.class);
		sceneModel = assetManager.loadModel("main.scene");
		sceneModel.setLocalScale(2.0f);
		rootNode.attachChild(sceneModel);

		flyCam.setMoveSpeed(7.5f);
		flyCam.setEnabled(false);

		registerInput();
		// setupCollision();
		setUpLight();
		setCam();
	}

	public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
		if (animName.equals("Walk")) {
			channel.setAnim("stand", 0.50f);
			channel.setLoopMode(LoopMode.DontLoop);
			channel.setSpeed(1f);
		}
	}

	public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
		// unused
	}

	private void setupCollision() {
		CollisionShape sceneShape = CollisionShapeFactory.createMeshShape(sceneModel);
		sceneBodyControl = new RigidBodyControl(sceneShape, 0);
		sceneModel.addControl(sceneBodyControl);
		bulletAppState.getPhysicsSpace().add(sceneBodyControl);

		CollisionShape actorShape = CollisionShapeFactory.createDynamicMeshShape(actor);
		actorControl = new RigidBodyControl(actorShape, 2);
		actor.addControl(actorControl);
		bulletAppState.getPhysicsSpace().add(actorControl);

		// CollisionShape capsuleShape =
		// CollisionShapeFactory.createBoxShape(actor);
		// charControl = new CharacterControl(capsuleShape, 0.05f);
		// charControl.setJumpSpeed(20);
		// charControl.setFallSpeed(30);
		// charControl.setGravity(30);
		// charControl.setPhysicsLocation(new Vector3f(0, 10, 0));
		// actor.addControl(charControl);
		// bulletAppState.getPhysicsSpace().add(charControl);
	}

	private void setUpLight() {
		viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));

		AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.White.mult(1.3f));
		rootNode.addLight(al);

		DirectionalLight dl = new DirectionalLight();
		dl.setColor(ColorRGBA.White);
		dl.setDirection(new Vector3f(2.8f, -2.8f, -2.8f).normalizeLocal());
		rootNode.addLight(dl);
	}

	private void setCam() {
		int camDist = 80;
		camNode = new CameraNode("Camera Node", cam);
		camNode.setControlDir(ControlDirection.SpatialToCamera);
		actor.attachChild(camNode);
		Vector3f actorPos = actor.getLocalTranslation();
		camNode.setLocalTranslation(actorPos.x, actorPos.y + camDist, actorPos.z - camDist);
		camNode.lookAt(actorPos, new Vector3f(0, 0.707f, 0.707f));
	}

	private void registerInput() {
		inputManager.addMapping("moveForward", new KeyTrigger(KeyInput.KEY_UP), new KeyTrigger(KeyInput.KEY_W));
		inputManager.addMapping("moveBackward", new KeyTrigger(KeyInput.KEY_DOWN), new KeyTrigger(KeyInput.KEY_S));
		inputManager.addMapping("Walk", new KeyTrigger(KeyInput.KEY_UP), new KeyTrigger(KeyInput.KEY_W));
		inputManager.addMapping("rotateRight", new KeyTrigger(KeyInput.KEY_RIGHT), new KeyTrigger(KeyInput.KEY_D));
		inputManager.addMapping("rotateLeft", new KeyTrigger(KeyInput.KEY_LEFT), new KeyTrigger(KeyInput.KEY_A));
		inputManager.addListener(new MyActionListener(), "moveForward", "moveBackward", "moveRight", "moveLeft");
		inputManager.addListener(new MyActionListener(), "rotateRight", "rotateLeft", "toggleRotate");
		inputManager.addListener(new MyActionListener(), "Walk");
	}

	class MyActionListener implements ActionListener {
		@Override
		public void onAction(String arg0, boolean arg1, float arg2) {
			direction.set(cam.getDirection()).normalizeLocal();
			if (arg0.equals("moveForward")) {
				direction.multLocal(50 * arg2).y = 0;
				actor.move(direction);
				// actorControl.setPhysicsLocation(actorControl.getPhysicsLocation().add(direction));
			}
			if (arg0.equals("moveBackward")) {
				direction.multLocal(-50 * arg2).y = 0;
				actor.move(direction);
				// actorControl.setPhysicsLocation(actorControl.getPhysicsLocation().add(direction));
			}
			if (arg0.equals("rotateRight") && arg1) {
				actor.rotate(0, -15 * arg2, 0);
			}
			if (arg0.equals("rotateLeft") && arg1) {
				actor.rotate(0, 15 * arg2, 0);
			}
			if (arg0.equals("Walk")) {
				if (!animChannel.getAnimationName().equals("Walk") && !arg1) {
					animChannel.setAnim("Walk", 0.50f);
					animChannel.setLoopMode(LoopMode.Loop);
				}
			}
		}
	}

	@Override
	public void simpleUpdate(float tpf) {
		super.simpleUpdate(tpf);
	}

	private class AnimEvtListenerImpl implements AnimEventListener {
		@Override
		public void onAnimChange(AnimControl arg0, AnimChannel arg1, String arg2) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onAnimCycleDone(AnimControl arg0, AnimChannel arg1, String arg2) {
			// TODO Auto-generated method stub

		}
	}

	public static void main(String[] args) {
		new ClientGui().start();
	}
}
