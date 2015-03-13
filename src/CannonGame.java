import javalib.funworld.*;
import javalib.worldcanvas.*;
import javalib.worldimages.*;
import java.util.*;
import javalib.tunes.*;
import javalib.colors.*;



public class CannonGame extends World {
	private static final double SPEED = .5;
	private static final int HEIGHT = 768;
	private static final int WIDTH = 768;
	private static final int NUM_SLOTS = 12;
	public static int slotLength = 64;
	private Grid grid;

	public static void main(String[] args) {
		CannonGame game = new CannonGame(CannonGame.generateGrid());
		game.bigBang(WIDTH, HEIGHT, SPEED);
	}

	public CannonGame(Grid grid) {
		super();
		this.grid = grid;
	}

	@Override
	public World onTick() {
		return this;
	}

	@Override
	public World onKeyEvent(String ke) {
		return new CannonGame(this.grid.onKeyEvent(ke));
	}

	@Override
	public World onMouseClicked(Posn p) {
		return new CannonGame(this.grid.onMouseClicked(p));
	}

	@Override
	public WorldImage makeImage() {
		return this.grid.makeImage();
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
	//input basic
	public static Grid generateGrid() {
		int width = CannonGame.slotLength;
		List<Posn> obstacles = new ArrayList<Posn>();
		obstacles.add(Utility.fromCoordinateToUpperLeftPosn(1, 1));
		obstacles.add(Utility.fromCoordinateToUpperLeftPosn(0,0));
		obstacles.add(Utility.fromCoordinateToUpperLeftPosn(7,7));
		List<Cannon> cannons = new ArrayList<Cannon>();
		LinkedList<Arrow> arrows = new LinkedList<Arrow>();
		List<Part> parts = new ArrayList<Part>();
		return new Grid(obstacles, cannons, arrows, parts, ClickType.ERASE);
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
	private static WorldImage BLANK = new CircleImage(new Posn(0,0), 0, new Red());
	//would be nice to someday make obstacle lists into BSTs
	private List<Posn> obstacles;
	private List<Cannon> cannons;
	private LinkedList<Arrow> arrows;
	private List<Part> parts;
	private ClickType clickType;
	private static final int radius = CannonGame.slotLength;

	public Grid(List<Posn> obstacles, List<Cannon> cannons, LinkedList<Arrow> arrows, List<Part> parts, ClickType clickType) {
		this.obstacles = obstacles;
		this.cannons = cannons;
		this.arrows = arrows;
		this.parts = parts;
		this.clickType = clickType;
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
	public WorldImage makeImage() {
		WorldImage img = makeObstacleImage(obstacles);
		return img.overlayImages(
				makeCannonImage(cannons),
				makeArrowImage(arrows),
				makePartImage(parts)
			);
	}
	public Grid onMouseClicked(Posn position) {
		Posn p = Utility.getNearestGridPosition(position);
		//for purity
		LinkedList<Arrow> copy = new LinkedList<Arrow>();
		copy.addAll(arrows);
		ArrayList<Integer> toRemove = new ArrayList<Integer>();

		//if clickType is erase, then remove the posn that's there
		if (this.clickType == ClickType.ERASE) {
			for (int i = arrows.size() - 1; i >= 0; i--) {
				if (posnEquals(arrows.get(i).pos, p)) {
					copy.remove(i);
				}
			}
		} else if (obstacleCollision(p)){
			//if in invalid location, do nothing
			return this;
		} else {
			//add new arrow
			Direction d = clickTypeToDirection(this.clickType);
			copy.add(new Arrow(p, d));
			if (arrows.size() > 4) {
				copy.removeFirst();
			}
		}

		//so pure
		return new Grid(this.obstacles, this.cannons, copy, this.parts, this.clickType);
	}
	public Grid onKeyEvent(String ke) {
		if (ke.equals("up") || ke.equals("w")) {
			return new Grid(this.obstacles, this.cannons, this.arrows, this.parts, ClickType.UP);
		} else if (ke.equals("right") || ke.equals("d")) {
			return new Grid(this.obstacles, this.cannons, this.arrows, this.parts, ClickType.RIGHT);
		} else if (ke.equals("left") || ke.equals("a")) {
			return new Grid(this.obstacles, this.cannons, this.arrows, this.parts, ClickType.LEFT);
		} else if (ke.equals("down") || ke.equals("s")) {
			return new Grid(this.obstacles, this.cannons, this.arrows, this.parts, ClickType.DOWN);
		} else if (ke.equals("x")) {
			return new Grid(this.obstacles, this.cannons, this.arrows, this.parts, ClickType.ERASE);
		} else {
			return this;
		}
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
	public Boolean obstacleCollision(Posn p) {
		Posn nextPosn = p;

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
	public Direction clickTypeToDirection(ClickType clickType) {
		if (clickType == ClickType.UP) {
			return Direction.UP;
		} else if (clickType == ClickType.LEFT) {
			return Direction.LEFT;
		} else if (clickType == ClickType.RIGHT) {
			return Direction.RIGHT;
		} else if (clickType == ClickType.DOWN) {
			return Direction.DOWN;
		} else {
			throw new RuntimeException("bad click type");
		}
	}

	public WorldImage makeObstacleImage(List<Posn> obstacles) {
		WorldImage obstacleImage = new FromFileImage(obstacles.get(0), "img/box.png");
		Posn current;
		if (obstacles.size() > 0) {
			for (int i = 1; i < obstacles.size(); i++) {
				current = obstacles.get(i);
				obstacleImage = new OverlayImages(obstacleImage,
									new FromFileImage(current, "img/box.png"));
			}
			return obstacleImage;
		} else {
			return BLANK;
		}
	}
	public WorldImage makeCannonImage(List<Cannon> cannons) {
		if (cannons.size() > 0) {
			WorldImage cannonImage = cannons.get(0).makeImage();
			for (int i = 1; i < cannons.size(); i++) {
				cannonImage = cannonImage.overlayImages(cannonImage, cannons.get(i).makeImage());
			}
			return cannonImage; 
		} else {
			return BLANK;
		}
	}
	public WorldImage makeArrowImage(LinkedList<Arrow> arrows) {
		if (arrows.size() > 0) {
			WorldImage arrowImage = arrows.get(0).makeImage();
			for (int i = 1; i < arrows.size(); i++) {
				arrowImage = arrowImage.overlayImages(arrowImage, arrows.get(i).makeImage());
			}
			return arrowImage;
		} else {
			return BLANK;
		}
	}
	public WorldImage makePartImage(List<Part> parts) {
		if (parts.size() > 0) {
			WorldImage partsImage = parts.get(0).makeImage();
			for (int i = 1; i < parts.size(); i++) {
				partsImage = partsImage.overlayImages(partsImage, parts.get(i).makeImage());
			}
			return partsImage;
		} else {
			return BLANK;
		}
	}

}

class Cannon {
	public Posn entrance;
	public Direction entranceDirection;
	public List<Posn> barrel;

	public WorldImage makeImage() {
		int radius = 100;
		return new FromFileImage(entrance, "img/cannon-entrance.png");
	}
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

	public WorldImage makeImage() {
		int radius = 100;
		return new FromFileImage(pos, this.getImagePath());
	}
//WATER, CANNON, CLOTH, GUNPOWDER, JUNK
	public String getImagePath() {
		if (this.type == PartType.WATER) {
			return "img/water.png";
		} else if (this.type == PartType.CANNON) {
			return "img/cannonball.img";
		} else if (this.type == PartType.CLOTH) {
			return "img/cloth.png";
		} else if (this.type == PartType.GUNPOWDER) {
			return "img/gunpowder.png";
		} else {//if (this.type == PartType.JUNK) {
			return "img/cloth.png";
		}
	}
}
class Arrow {
	public Posn pos;
	public Direction direction;

	public Arrow (Posn pos, Direction direction) {
		this.pos = pos;
		this.direction = direction;
	}

	public WorldImage makeImage() {
		int radius = 100;
		return new FromFileImage(this.pos, this.getImagePath());
	}

	public String getImagePath() {
		if (this.direction == Direction.UP) {
			return "img/arrow-up.png";
		} else if (this.direction == Direction.RIGHT) {
			return "img/arrow-right.png";
		} else if (this.direction == Direction.LEFT) {
			return "img/arrow-left.png";
		} else if (this.direction == Direction.DOWN) {
			return "img/arrow-down.png";
		} else {
			return null;
		}
	}
}

class Utility {
	public static Posn getNearestGridPosition(Posn p) {
		int x = p.x/64 * 64 + (int)Math.ceil(.5*CannonGame.slotLength);
		int y = p.y/64 * 64 + (int)Math.ceil(.5*CannonGame.slotLength);
		return new Posn(x, y);
	}

	public static Posn getNearestGridPosition(int x, int y) {
		return getNearestGridPosition(new Posn(x, y));
	}

	public static Posn fromCoordinateToUpperLeftPosn(int x, int y) {
		int newX = x*CannonGame.slotLength + (int)Math.ceil(.5*CannonGame.slotLength);
		int newY = y*CannonGame.slotLength + (int)Math.ceil(.5*CannonGame.slotLength);
		return new Posn(newX, newY);
	}
}