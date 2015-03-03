import javalib.funworld.*;
import javalib.worldcanvas.*;
import javalib.worldimages.*;

public class CannonGame extends World {

	//@Override
	public World onTick() {
		return this;
	}

	//@Override
	public World onKeyEvent(String ke) {
		return this;
	}

	//@Override
	public World onMouseClicked(Posn p) {
		return this;
	}

	

	@Override
	public WorldImage makeImage() {
		return new FrameImage(new Posn(100, 100), 100, 300, java.awt.Color.RED);
	}
}