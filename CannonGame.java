import javalib.funworld.*;
import javalib.worldcanvas.*;
import javalib.worldimages.*;
import java.util.*;

public class CannonGame extends World {
	private final int SCALE = 1;
	private final int HEIGHT = 400;
	private final int WIDTH = 400;
	private Grid grid;
	private ClickType clickType;

	public static void main(String[] args) {

	}

	public CannonGame(Grid grid, ClickType clickType) {
		this.grid = grid;
		this.clickType = clickType;
	}

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
		return new CannonGame(this.grid.onMouseClicked(p, this.clickType), clickType);
	}

	@Override
	public WorldImage makeImage() {
		return new FrameImage(new Posn(100, 100), 100, 300, java.awt.Color.RED);
	}
}

//enums
enum ClickType {
	UP, DOWN, LEFT, RIGHT, ERASE
}
enum Direction {
	UP, DOWN, LEFT, RIGHT
}
enum PartType {
	WATER, CANNON, CLOTH, GUNPOWDER, JUNK
}

//Game component classes -- the Model
class Grid {
	private List<Posn> obstacles;
	private List<Cannon> cannons;
	private Queue<Arrow> arrows;
	private List<Part> parts;

	//Mirroring world functions
	public Grid onTick() {

		//here we see where the parts want to go, check if they're movable
		for (Part p : this.parts) {
			if (obstacleCollision(p)) {
				Direction d = findNextValidDirection(p);
				Posn pos = nextPosn(p.pos, d);

				//will the below change this.parts? do i need to just build
				//an entirely new list?
				p = new Part(p.type, pos, findNextValidDirection(p));
			}
		}

		//to be changed
		return this;
	}
	public WorldImage makeImage() {
		return new FrameImage(new Posn(100, 100), 100, 300, java.awt.Color.RED);
	}
	public Grid onMouseClicked(Posn p, ClickType clickType) {
		return this;
	}

	//Functions to process Part p in terms of Grid context
	public Boolean obstacleCollision(Part p) {
		for (Posn pos : this.obstacles) {
			if (p.pos.x == pos.x && p.pos.y == pos.y) {
				return true;
			}
		}
		for (Cannon c : this.cannons) {
			for (Posn pos : c.barrel) {
				if (p.pos.x == pos.x && p.pos.y == pos.y)
					return true;
			}
		}
		return false;
	}
	public Direction findNextValidDirection(Part p) {
		return Direction.UP;
	}
	public Posn nextPosn(Posn p, Direction d) {
		return new Posn(0, 0);
	}

}

class Cannon {
	public Posn entrance;
	public Direction entranceDirection;
	public List<Posn> barrel;
}
class Part {
	public PartType type;
	public Posn pos;
	public Direction direction;

	public Part(PartType type, Posn pos, Direction direction) {
		this.type = type;
		this.pos = pos;
		this.direction = direction;
	}
}
class Arrow {

}