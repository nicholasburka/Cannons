import javalib.funworld.*;
import javalib.worldcanvas.*;
import javalib.worldimages.*;
import java.util.*;
import javalib.tunes.*;



public class CannonGame extends World {
	private static final double SPEED = .5;
	private static final int HEIGHT = 400;
	private static final int WIDTH = 400;
	private static final int NUM_SLOTS = 32;
	private int gridSlotHeight = HEIGHT/NUM_SLOTS;
	private int gridSlotWidth = HEIGHT/NUM_SLOTS;
	private Grid grid;
	private ClickType clickType;

	public static void main(String[] args) {
		CannonGame game = new CannonGame(CannonGame.generateGrid(), ClickType.ERASE);
		game.bigBang(WIDTH, HEIGHT, SPEED);
	}

	public CannonGame(Grid grid, ClickType clickType) {
		super();
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

	//experimenting with music
	public Chord randomChord() {
		Chord chord1 = new Chord("C4n1", "A3n1", "E3n1", "G4n1");
		Tune tune = new Tune(0);
		tune.addChord(chord1);
		MusicBox music = new MusicBox();
		music.playTune(tune);

		int rand = (int)Math.floor((int)Math.random()*3);
		switch (rand) {
			case 0:
				return chord1;
			case 1:
				return chord1;
			case 2:
				return chord1;
		}
		return chord1;
	}

	//just make a super simple grid, for now
	public static Grid generateGrid() {
		List<Posn> obstacles = new ArrayList<Posn>();
		obstacles.add(new Posn(0,0));
		obstacles.add(new Posn(1,1));
		List<Cannon> cannons = new ArrayList<Cannon>();
		Queue<Arrow> arrows = new LinkedList<Arrow>();
		List<Part> parts = new ArrayList<Part>();
		return new Grid(obstacles, cannons, arrows, parts);
	}
}

//enums
enum ClickType {
	UP, DOWN, LEFT, RIGHT, ERASE
}
enum Direction {
	UP, DOWN, LEFT, RIGHT, NULL
}
enum PartType {
	WATER, CANNON, CLOTH, GUNPOWDER, JUNK
}

//Game component classes -- the Model
class Grid {
	//would be nice to someday make obstacle lists into BSTs
	private List<Posn> obstacles;
	private List<Cannon> cannons;
	private Queue<Arrow> arrows;
	private List<Part> parts;

	public Grid(List<Posn> obstacles, List<Cannon> cannons, Queue<Arrow> arrows, List<Part> parts) {
		this.obstacles = obstacles;
		this.cannons = cannons;
		this.arrows = arrows;
		this.parts = parts;
	}

	//Mirroring world functions
	public Grid onTick() {

		Arrow arrow;
		//here we see where the parts want to go, check if they're movable
		for (Part p : this.parts) {
			arrow = onArrow(p);
			if (arrow.direction == Direction.NULL) {

				p = new Part(p.type, p.pos, arrow.direction);
			}
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
	public WorldImage makeImage(int scale) {
		return new FrameImage(new Posn(100, 100), 100, 300, java.awt.Color.RED);
	}
	public Grid onMouseClicked(Posn p, ClickType clickType) {
		return this;
	}

	//Functions to process Part p in terms of Grid context
	public Boolean obstacleCollision(Part p) {
		Posn nextPosn = nextPosn(p.pos, p.direction);

		for (Posn pos : this.obstacles) {
			if (posnEquals(pos, nextPosn)) {
				return true;
			}
		}
		for (Cannon c : this.cannons) {
			for (Posn pos : c.barrel) {
				if (posnEquals(pos, nextPosn))
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
	public Arrow onArrow(Part p) {
		for (Arrow a : this.arrows) {
			if (posnEquals(a.pos, p.pos)) {
				return a;
			}
		}
		return new Arrow(new Posn(0,0), Direction.NULL);
	}
	public Boolean posnEquals(Posn p1, Posn p2) {
		return (p1.x == p2.x && p1.y == p2.y);
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
	public Posn pos;
	public Direction direction;

	public Arrow (Posn pos, Direction direction) {
		this.pos = pos;
		this.direction = direction;
	}
}