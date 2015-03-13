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
	public static final int NUM_SLOTS = 12;
	public static int slotLength = 64;
	public static int score = 0;
	public static int ticks = 0;
	public static String systemMessage = "";
	private Grid grid;

	public static void main(String[] args) {
		CannonGame game = new CannonGame(CannonGame.generateGrid());
		Tester tester = new Tester();
		tester.runTests();
		game.bigBang(WIDTH, HEIGHT, SPEED);
	}

	public CannonGame(Grid grid) {
		super();
		this.grid = grid;
	}

	@Override
	public World onTick() {
		CannonGame.score--;
		CannonGame.systemMessage = "-1";
		//System.out.println("Score is: " + CannonGame.score);
		return new CannonGame(this.grid.onTick());
	}

	@Override
	public World onKeyEvent(String ke) {
		if (ke.equals("q")) {
			return endOfWorld("Your final score was: " + CannonGame.score + "\nThanks for playing!");
		} else return new CannonGame(this.grid.onKeyEvent(ke));
	}

	@Override
	public World onMouseClicked(Posn p) {
		return new CannonGame(this.grid.onMouseClicked(p));
	}

	@Override
	public WorldImage makeImage() {
		return this.grid.makeImage().overlayImages(new TextImage(new Posn(70, HEIGHT - 20), "Score: " + CannonGame.score, new Blue()),
			new TextImage(new Posn(93, HEIGHT - 35), CannonGame.systemMessage, new Red()));
	}

	public WorldImage lastImage(String s) {
		return new TextImage(new Posn(HEIGHT/2, WIDTH/2), s, new Blue());
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
		//obstacles.add(Utility.fromCoordinateToUpperLeftPosn(1, 1));
		//obstacles.add(Utility.fromCoordinateToUpperLeftPosn(0,0));
		//obstacles.add(Utility.fromCoordinateToUpperLeftPosn(11,7));
		List<Cannon> cannons = new ArrayList<Cannon>();
		cannons.add(makeCannon(Utility.fromCoordinateToUpperLeftPosn(5,5), Direction.RIGHT));
		LinkedList<Arrow> arrows = new LinkedList<Arrow>();
		List<Part> parts = new ArrayList<Part>();

		//parts.add(new Part(PartType.WATER,Utility.fromCoordinateToUpperLeftPosn(1,3),Direction.RIGHT));
		//parts.add(new Part(PartType.CANNON,Utility.fromCoordinateToUpperLeftPosn(6,3),Direction.RIGHT));
		Grid g = new Grid(obstacles, cannons, arrows, parts, ClickType.ERASE);
		for (int i = 0; i < 22; i++) {
			obstacles.add(g.getRandomEmptyPosn());
		}
		for (int i = 0; i < 3; i++) {
			parts.add(new Part(PartType.CANNON, g.getRandomEmptyPosn(), Direction.DOWN));
			g = new Grid(obstacles, cannons, arrows, parts, ClickType.ERASE);
		}
		for (int i = 0; i < 3; i++) {
			parts.add(new Part(PartType.CLOTH, g.getRandomEmptyPosn(), Direction.DOWN));
			g = new Grid(obstacles, cannons, arrows, parts, ClickType.ERASE);
		}
		for (int i = 0; i < 4; i++) {
			parts.add(new Part(PartType.WATER, g.getRandomEmptyPosn(), Direction.DOWN));
			g = new Grid(obstacles, cannons, arrows, parts, ClickType.ERASE);
		}
		for (int i = 0; i < 3; i++) {
			parts.add(new Part(PartType.GUNPOWDER, g.getRandomEmptyPosn(), Direction.DOWN));
			g = new Grid(obstacles, cannons, arrows, parts, ClickType.ERASE);
		}
		return g;
	}

	public static Cannon makeCannon(Posn p, Direction d) {
		List<Posn> barrel = new ArrayList<Posn>();
		Posn n = Grid.nextPosn(p, d);
		barrel.add(n);
		barrel.add(Grid.nextPosn(n, d));
		Cannon c = new Cannon(p, d, barrel, new Stack<Part>());
		return c;
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
		List<Part> newParts = new ArrayList<Part>();
		Arrow arrow;
		Part temp;
		List<Cannon> newCannons = this.cannons;
		//here we see where the parts want to go, check if they're movable
		for (Part p : this.parts) {

			arrow = onArrow(p);
			if (arrow.direction != Direction.NULL) {
				//If on arrow, then change derction of part
				temp = new Part(p.type, nextPosn(p.pos, arrow.direction), arrow.direction);
				newParts.add(temp);
			} else if (obstacleCollision(p) || offBoard(p)/* || 
				(cannonEntrance(p, this.cannons).isFull() && cannonEntrance(p, this.cannons).entranceDirection != Direction.NULL) ||
				partCollision(p)*/) {
				//if collision, turn part
				Direction d = findNextValidDirection(p);
				Posn pos = nextPosn(p.pos, d);
				temp = new Part(p.type, pos, d);
				newParts.add(temp);
			} else if (cannonEntrance(p, this.cannons).entranceDirection != Direction.NULL) {
				//if entering the cannon
				Cannon cannon = cannonEntrance(p, this.cannons);
				cannon.insertPart(p);
				newParts.add(new Part(p.type, this.getRandomEmptyPosn(), Direction.DOWN));
				List<Cannon> oldCannons = newCannons;
				newCannons = new ArrayList<Cannon>();
				//don't add temp to newParts, now the cannon owns this
				for (Cannon c : oldCannons) {
					//System.out.println("OC position of old cannon: " + c.entrance.x + " " + c.entrance.y);
					//System.out.println("OC position of new cannon: " + cannon.entrance.x + " " + cannon.entrance.y);
					if (posnEquals(c.entrance, cannon.entrance)) {
						newCannons.add(cannon);
						//System.out.println("OC just added new cannon with num parts: " + cannon.contents.size());
						////System.out.println("just added new cannons in before: " + newCannons.size());
					} else {
						newCannons.add(c);
					}
				}
			} else {
				temp = new Part(p.type, nextPosn(p.pos, p.direction), p.direction);
				newParts.add(temp);
			}
		}

		Iterator<Part> iter = newParts.iterator();
		ArrayList<Part> toAdd = new ArrayList<Part>();
		while (iter.hasNext()) {
			temp = iter.next();
			if (isOnCannonEntrance(temp, newCannons).entranceDirection != Direction.NULL) {
				//if entering the cannon
				////System.out.println("CANNONENTRANCE - AFTER");
				Cannon cannon = isOnCannonEntrance(temp, newCannons);
				cannon.insertPart(temp);
				toAdd.add(temp);
				//System.out.println(cannon.entrance.x + " " + cannon.entrance.y);
				//don't add temp to newParts, now the cannon owns this
				for (Cannon c : newCannons) {
					//System.out.println("TC position of old cannon: " + c.entrance.x + " " + c.entrance.y);
					//System.out.println("TC position of new cannon: " + cannon.entrance.x + " " + cannon.entrance.y);
					if (posnEquals(c.entrance, cannon.entrance)) {
						removeFromCannons(c.entrance, newCannons);
						newCannons.add(cannon);
						//System.out.println("TC just added new cannon with num parts: " + cannon.contents.size());
					}
				}
				iter.remove();
				////System.out.println("just removed current part:" + newCannons.size());
			}
		}
		////System.out.println("regular it, right before return: " + newCannons.size());
		for (int i = 0; i < toAdd.size(); i++) {
			newParts.add(new Part(toAdd.get(i).type, this.getRandomEmptyPosn(), Direction.DOWN));
		}
		//to be changed
		return new Grid(this.obstacles, newCannons, this.arrows, newParts, this.clickType);
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
		} else if (onArrow(p).direction == Direction.NULL) {
			Direction d = clickTypeToDirection(this.clickType);
			//add new arrow
			copy.add(new Arrow(p, d));
			if (arrows.size() > 4) {
				copy.removeFirst();
			}
		} else {
			Direction d = clickTypeToDirection(this.clickType);
			Iterator<Arrow> iter = copy.iterator();
			while (iter.hasNext()) {
				Arrow a = iter.next();
				if (posnEquals(a.pos, onArrow(p).pos)) {
					iter.remove();
				}
			}
			copy.add(new Arrow(p, d));
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
	public Boolean partCollision(Part p) {
		Posn nextPosn = nextPosn(p.pos, p.direction);
		for (Part otherP : this.parts) {
			if (posnEquals(nextPosn, otherP.pos) || posnEquals(nextPosn, nextPosn(otherP.pos, otherP.direction))) {
				return true;
			}
		}
		return false;
	}
	public void removeFromCannons(Posn p, List<Cannon> cannons) {
		Iterator<Cannon> iter = cannons.iterator();
		Cannon current;
		while (iter.hasNext()) {
			current = iter.next();
			if (posnEquals(current.entrance, p)) {
				iter.remove();
			}
		}
	}
	public Boolean offBoard(Part p) {
		Posn nextPosn = nextPosn(p.pos, p.direction);

		return (nextPosn.x <= 0 || nextPosn.x >= (CannonGame.NUM_SLOTS)*64 ||
			nextPosn.y <= 0 || nextPosn.y >= (CannonGame.NUM_SLOTS)*64) ;
	}
	public Direction findNextValidDirection(Part p) {
		Part newP = new Part(p.type, p.pos, Utility.getNextDirection(p.direction));
		int numDirections = 4;
		for (int i = 0; i < numDirections; i++) {
			if (obstacleCollision(newP) || offBoard(newP)) {
				newP = new Part(p.type, p.pos, Utility.getNextDirection(newP.direction));
			} else if (partCollision(newP)) {
				newP = new Part(p.type, p.pos, Utility.getOppositeDirection(newP.direction));
			}
		}
		return newP.direction;
	}
	public static Posn nextPosn(Posn p, Direction d) {
		int dx;
		int dy;
		if (d == Direction.UP) {
			dx = 0;
			dy = -64;
		} else if (d == Direction.LEFT) {
			dx = -64;
			dy = 0;
		} else if (d == Direction.DOWN) {
			dx = 0;
			dy = 64;
		} else if (d == Direction.RIGHT) {
			dx = 64;
			dy = 0;
		} else {
			return p;
		}
		return new Posn(p.x + dx, p.y + dy);
	}
	public Arrow onArrow(Part p) {
		for (Arrow a : this.arrows) {
			if (posnEquals(a.pos, p.pos)) {
				return a;
			}
		}
		return new Arrow(new Posn(0,0), Direction.NULL);
	}
	public Arrow onArrow(Posn p) {
		for (Arrow a : this.arrows) {
			if (posnEquals(a.pos, p)) {
				return a;
			}
		}
		return new Arrow(new Posn(0,0), Direction.NULL);
	}
	public Cannon cannonEntrance(Part p, List<Cannon> cannons) {
		Direction d = p.direction;
		for (Cannon c : cannons) {
				////System.out.println(c.entrance.x);
				////System.out.println(p.pos.x);
			if (posnEquals(nextPosn(p.pos, p.direction), c.entrance)) {
				//System.out.println("TRUE");
				Cannon can = new Cannon(c.entrance, c.entranceDirection, c.barrel, c.contents);
				//System.out.println("cans stuff: " + c.entrance.x + " " + c.entrance.y);
				return can;
			}
		}
		List<Posn> list = new ArrayList<Posn>();
		list.add(new Posn(0,0));
		list.add(new Posn(0,0));
		return new Cannon(new Posn(0,0), Direction.NULL, list, new Stack<Part>());
	}
	public Cannon isOnCannonEntrance(Part p, List<Cannon> cannons) {
		Direction d = p.direction;
		for (Cannon c : cannons) {
			//System.out.println("position of part: " + p.pos.x + " " + p.pos.y);
			//System.out.println("position of cannon: " + c.entrance.x + " " + c.entrance.y);
				//System.out.println(c.entrance.x);
				//System.out.println(p.pos.x);
			//System.out.println(posnEquals(p.pos, c.entrance));
			if (posnEquals(p.pos, c.entrance)) {
				//System.out.println("TRUE");
				Cannon can = new Cannon(c.entrance, c.entranceDirection, c.barrel, c.contents);
				//System.out.println("cans stuff: " + c.entrance.x + " " + c.entrance.y);
				return can;
			}
		}
		List<Posn> list = new ArrayList<Posn>();
		list.add(new Posn(0,0));
		list.add(new Posn(0,0));
		return new Cannon(new Posn(0,0), Direction.NULL, list, new Stack<Part>());
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

	public Boolean isValidObstacle(Posn obs) {
		for (Cannon c : this.cannons) {
			if (posnEquals(obs, nextPosn(c.entrance, Utility.getOppositeDirection(c.entranceDirection))) || 
				posnEquals(obs, c.entrance)) {
				return false;
			}
			for (Posn p : c.barrel) {
				if (posnEquals(obs, p)) {
					return false;
				}
			}
		}
		for (Posn p : this.obstacles) {
			if (posnEquals(p, obs)) {
				return false;
			}
		}

		return true;
	}

	public Posn getRandomEmptyPosn() {
		int randx = (int)Math.floor(Math.random()*CannonGame.NUM_SLOTS);
		int randy = (int)Math.floor(Math.random()*CannonGame.NUM_SLOTS);
		Posn pos = Utility.fromCoordinateToUpperLeftPosn(randx, randy);
		if (isValidObstacle(pos)) {
			for (Part p : this.parts) {
				if (posnEquals(pos, p.pos)) {
					return getRandomEmptyPosn();
				}
			}
			return pos;
		} else {
			return getRandomEmptyPosn();
		}
	}
}

class Cannon {
	public Posn entrance;
	//the direction the cannon should be entered from (inward)
	public Direction entranceDirection;
	public List<Posn> barrel;
	public Stack<Part> contents;
	public Boolean full;
	public Cannon(Posn entrance, Direction entranceDirection, List<Posn> barrel, Stack<Part> contents) {
		this.entrance = entrance;
		this.entranceDirection = entranceDirection;
		if (barrel.size() < 2) {
			throw new RuntimeException("barrel initialized wrong");
		}
		this.barrel = barrel;
		this.contents = contents;
	}
	public WorldImage makeImage() {
		int radius = 100;
		return new FromFileImage(entrance, "img/cannon-entrance.png").overlayImages(
				makeContentsImage(),
				new FromFileImage(barrel.get(0), "img/cannon-entrance.png"),
				new FromFileImage(barrel.get(1), getImagePath(barrel.get(1)))
			);
	}

	public String getImagePath(Posn barrel) {
		if (this.entranceDirection == Direction.RIGHT) {
			return "img/cannon-end-right.png";
		} else {
			return "img/cannon-end-left.png";
		}
	}

	public WorldImage makeContentsImage() {
		//System.out.println(this.contents.size());
		Stack<Part> copy = new Stack<Part>();
		copy.addAll(this.contents);
		WorldImage img = new CircleImage(new Posn(0,0), 0, new Blue());
		while (copy.size() > 0) {
			img.overlayImages(copy.pop().makeImage());
		}
		return img;
	}

	public Cannon insertPart(Part p) {
		//System.out.println("INSERTING");
		//System.out.println("Num elements before: " + this.contents.size());
		/*Part newPart = new Part(p.type, p.pos, Direction.NULL);
		if (p.type == PartType.WATER) {
			this.contents = new Stack<Part>();
			this.full = (this.contents.size() > 2);
		} else {
			this.full = (this.contents.size() + 1 > 2);
			switch (this.contents.size()) {
				case 3:
					throw new RuntimeException("weird");
				case 0:
					newPart = new Part(p.type, barrel.get(1), Direction.NULL);
					break;
				case 1:
					newPart = new Part(p.type, barrel.get(0), Direction.NULL);
					break;
				case 2:
					newPart = new Part(p.type, this.entrance, Direction.NULL);
					break;
			}
			this.contents.push(newPart);
		}
		//System.out.println("Num elements after: " + this.contents.size());*/
		if (p.type == PartType.WATER) {
			//System.out.println("WATER! -50 to your score.");
			CannonGame.systemMessage = "Water! -50";
			CannonGame.score = CannonGame.score - 50;
		} else {
			CannonGame.score = CannonGame.score + 20;
			CannonGame.systemMessage = "Nice! +20";
		}
		this.contents.add(p);
		return new Cannon(this.entrance, this.entranceDirection, this.barrel, this.contents);
	}

	public Boolean isFull() {
		return this.full;
	}

	public Boolean isCorrect() {
		Stack<Part> copy = new Stack<Part>();
		copy.addAll(this.contents);
		return (copy.pop().type == PartType.CANNON && copy.pop().type == PartType.CLOTH
			&& copy.pop().type == PartType.GUNPOWDER);
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
			return "img/cannonball.png";
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

	public static Direction getOppositeDirection(Direction d) {
		if (d == Direction.UP) {
			return Direction.DOWN;
		} else if (d == Direction.DOWN) {
			return Direction.UP;
		} else if (d == Direction.RIGHT) {
			return Direction.LEFT;
		} else if (d == Direction.LEFT) {
			return Direction.RIGHT;
		} else {
			return d;
		}
	}
	public static Direction getNextDirection(Direction d) {
		if (d == Direction.UP) {
			return Direction.RIGHT;
		} else if (d == Direction.DOWN) {
			return Direction.LEFT;
		} else if (d == Direction.RIGHT) {
			return Direction.DOWN;
		} else if (d == Direction.LEFT) {
			return Direction.UP;
		} else {
			return d;
		}
	}

	/*public static List<T implements Posnable> removeByPosn(List<T implements Posna> list, Posn p) {
		Iterator<T> iter = list.iterator() 
		T current;
		while (iter.hasNext()) {
			current = list.next();
			if (current)
		}
	}*/
}

class Tester {
	public Tester(){}

	public void runTests() {
		System.out.println("Running isItAGame()...");
		System.out.println("It's definitely a game.");

	}
}