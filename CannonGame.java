import javalib.funworld.*;
import javalib.worldcanvas.*;
import javalib.worldimages.*;

public class CannonGame extends World {
	private int scale;

	@Override
	public World onTick() {
		return this;
	}

	@Override
	public World onKeyEvent(String ke) {
		return this;
	}

	@Override
	public World onMouseClicked(Posn p) {
		return this;
	}

	

	@Override
	public WorldImage makeImage() {
		return new FrameImage(new Posn(100, 100), 100, 300, java.awt.Color.RED);
	}
}


abstract class GamePiece {
	public String imagePath;
	public Posn position;

	//returns true if the GamePiece should be visible, meaning it is "on top"
	public abstract Boolean visible(); 

}

abstract class MovingPart extends GamePiece {
	public String type;
	public int dx;
	public int dy;

}
/*
abstract class Tile extends GamePiece {

}*/