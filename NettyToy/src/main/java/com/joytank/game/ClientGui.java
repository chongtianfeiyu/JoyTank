package com.joytank.game;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * Main GUI of the client side, this needs to replace the simple
 * {@link ClientUi}
 * 
 * @author lizhaoliu
 */
public class ClientGui extends SimpleApplication {

  private Spatial sceneModel;
  
  private Node actor;

  @Override
  public void simpleInitApp() {
    assetManager.registerLocator("assets/models/Oto.zip", ZipLocator.class);
    actor = (Node) assetManager.loadModel("Oto.mesh.xml");
    actor.setLocalScale(0.5f);
    actor.move(0, 1.0f, 0);
    rootNode.attachChild(actor);
    
    assetManager.registerLocator("assets/models/town.zip", ZipLocator.class);
    sceneModel = assetManager.loadModel("main.scene");
    sceneModel.setLocalScale(2.0f);
    rootNode.attachChild(sceneModel);
    
    setUpLight();
  }

  private void setUpLight() {
    // We add light so we see the scene
    AmbientLight al = new AmbientLight();
    al.setColor(ColorRGBA.White.mult(1.3f));
    rootNode.addLight(al);

    DirectionalLight dl = new DirectionalLight();
    dl.setColor(ColorRGBA.White);
    dl.setDirection(new Vector3f(2.8f, -2.8f, -2.8f).normalizeLocal());
    rootNode.addLight(dl);
  }

  public static void main(String[] args) {
    new ClientGui().start();
  }
}
