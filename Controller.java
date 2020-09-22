package com.intershala.connectfour;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Controller implements Initializable {

	@FXML
	public GridPane rootGridPane;
	@FXML
	public Pane inserteddiscpane;
	@FXML
	public Label playernamelabel;
	@FXML
	public TextField p1;
	@FXML
	public TextField p2;
	@FXML
	public Button setName;

	private static final int COLUMNS=7;
	private static final int ROWS= 6;
	private static final int CIRCLE_DIAMETER=80;
	private static final String discColor1="#24303e";
	private static final String discColor2="#4caa88";

	private static String PLAYER_ONE= "Player 1";
	private static String PLAYER_TWO= "Player 2";

	private boolean isPlayerOneTurn= true;
	private boolean isAllowedToEnter = true;

	private Disc [][] insertedDiscArray = new Disc[ROWS][COLUMNS];



	public void createPlayground() {

		Shape rectangleWithHoles= createGameStructuralGrid();
		rootGridPane.add(rectangleWithHoles,0,1);

		List<Rectangle> rectangleList=createClickableColumns();
		for (Rectangle rectangle: rectangleList) {
			rootGridPane.add(rectangle, 0, 1);
		}
		setName.setOnAction(event -> {
			PLAYER_ONE = p1.getText();
			PLAYER_TWO = p2.getText();
			playernamelabel.setText(PLAYER_ONE);
		});
	}


	private Shape createGameStructuralGrid(){



		Shape rectangleWithHoles= new Rectangle((COLUMNS+1)*CIRCLE_DIAMETER,(ROWS+1)*CIRCLE_DIAMETER);

		for(int row= 0;row<ROWS;row++){
			for(int col=0; col<COLUMNS;col++){

				Circle circle= new Circle();
				circle.setRadius(CIRCLE_DIAMETER/2);
				circle.setCenterX(CIRCLE_DIAMETER/2);
				circle.setCenterY(CIRCLE_DIAMETER/2);
				circle.setSmooth(true);

				circle.setTranslateX(col*(CIRCLE_DIAMETER+5)+ CIRCLE_DIAMETER/4);
				circle.setTranslateY(row*(CIRCLE_DIAMETER+5)+ CIRCLE_DIAMETER/4);

				rectangleWithHoles= Shape.subtract(rectangleWithHoles,circle);


			}
		}

		rectangleWithHoles.setFill(Color.WHITE);
		return rectangleWithHoles;

	}

	private List<Rectangle> createClickableColumns(){

		List<Rectangle> rectangleList= new ArrayList<>();


		for(int col=0;col<COLUMNS;col++){
			Rectangle rectangle = new Rectangle(CIRCLE_DIAMETER,(ROWS+1)*CIRCLE_DIAMETER);
			rectangle.setFill(Color.TRANSPARENT);
			rectangle.setTranslateX((col*(CIRCLE_DIAMETER+5)+CIRCLE_DIAMETER/4));

			rectangle.setOnMouseEntered(event -> rectangle.setFill(Color.valueOf("#eeeeee26")));
			rectangle.setOnMouseExited(event -> rectangle.setFill(Color.TRANSPARENT));

			final int column = col;
			rectangle.setOnMouseClicked(event -> {

				if(isAllowedToEnter){

					isAllowedToEnter=false; //Stopping multiple drop of disk
					insertDisc(new Disc(isPlayerOneTurn), column);

				}

			});

			rectangleList.add(rectangle);
		}


		return rectangleList;
	}

	private void insertDisc (Disc disc,int column){



		int row =ROWS-1;
		while(row >=0){

			if(getDiscIfPresent(row,column)==null){
				break;
			}
			row--;

		}

		if(row<0){
			return;
		}


		insertedDiscArray[row][column]=disc; //for structural changes
		inserteddiscpane.getChildren().add(disc);  // for visual changes

		disc.setTranslateX(column*(CIRCLE_DIAMETER+5)+ CIRCLE_DIAMETER/4);

		int currentRow=row;
		TranslateTransition translateTransition= new TranslateTransition(Duration.seconds(0.5),disc);
		translateTransition.setToY(row*(CIRCLE_DIAMETER+5)+ CIRCLE_DIAMETER/4);
		translateTransition.setOnFinished(event -> {

			isAllowedToEnter = true;
			if(gameEnded(currentRow,column)){
				gameOver();
				return;
			}

			isPlayerOneTurn = !isPlayerOneTurn;
			playernamelabel.setText(isPlayerOneTurn? PLAYER_ONE:PLAYER_TWO);
		});

		translateTransition.play();

	}

	private boolean gameEnded(int row, int column){

		//vertical points

		List<Point2D> verticalPoints =IntStream.rangeClosed(row-3,row+3)  //range of values=0,1,2,3,4,5
										.mapToObj(r-> new Point2D(r,column)) //0,3 1,3 2,3 3,3 4,3 5,3
										.collect(Collectors.toList());

		//horizontal points

		List<Point2D> horizontalPoints=IntStream.rangeClosed(column-3,column+3)
										.mapToObj(col-> new Point2D(row,col))
										.collect(Collectors.toList());

		//diagonal1

		Point2D starPoint1 = new Point2D(row-3,column+3);
		List<Point2D> diagonal1Points = IntStream.rangeClosed(0,6)
										.mapToObj(i-> starPoint1.add(i,-i))
										.collect(Collectors.toList());

		//diagonal2

		Point2D starPoint2 = new Point2D(row-3,column-3);
		List<Point2D> diagonal2Points = IntStream.rangeClosed(0,6)
										.mapToObj(i-> starPoint2.add(i,i))
										.collect(Collectors.toList());


		boolean isEnded= checkCombination(verticalPoints ) || checkCombination(horizontalPoints)
							||checkCombination(diagonal1Points) ||checkCombination(diagonal2Points);
		return isEnded;

	}

	private boolean checkCombination(List<Point2D> points) {

		int chain = 0;

		for (Point2D point: points ){
			int rowIndexForArray= (int) point.getX();
			int columnIndexForArray= (int) point.getY();

			Disc disc= getDiscIfPresent(rowIndexForArray,columnIndexForArray);

			if(disc !=null && disc.isPlayerOneMove==isPlayerOneTurn) {

				chain++;
				if (chain == 4) {
					return true;
				}

			}else{

					chain=0;
				}

		}
		return false;

	}

	private Disc getDiscIfPresent(int row,int column){

		if(row>=ROWS|| row<0 || column>=COLUMNS|| column<0){
			return null;
		}

		return insertedDiscArray[row][column];
	}

	private void gameOver() {

		String winner = isPlayerOneTurn ? PLAYER_ONE : PLAYER_TWO ;
		Alert alert = new Alert((Alert.AlertType.INFORMATION));
		alert.setTitle("Connect Four");
		alert.setHeaderText("Winner is: " + winner);
		alert.setContentText("Want to play again? ");

		ButtonType yesBtn = new ButtonType("Yes");
		ButtonType noBtn = new ButtonType("No, Exit");
		alert.getButtonTypes().setAll(yesBtn, noBtn);

		Platform.runLater(() -> {

			Optional<ButtonType> btnClicked = alert.showAndWait();
			if (btnClicked.isPresent() && btnClicked.get() == yesBtn) {
				resetGame();

			} else {
				Platform.exit();
				System.exit(0);

			}

		});
	}


	public void resetGame() {

		inserteddiscpane.getChildren().clear();

		for (int row=0 ; row<insertedDiscArray.length;row++){
			for (int col=0 ; col<insertedDiscArray.length; col++){
				insertedDiscArray[row][col]=null;
			}
		}
		isPlayerOneTurn=true;
		playernamelabel.setText("None");

		createPlayground();
	}

	private static class Disc extends Circle{

		private final boolean isPlayerOneMove;

		public Disc(boolean isPlayerOneMove){
			this.isPlayerOneMove= isPlayerOneMove;
			setRadius(CIRCLE_DIAMETER/2);
			setFill(isPlayerOneMove? Color.valueOf(discColor1):Color.valueOf(discColor2) );
			setCenterX(CIRCLE_DIAMETER/2);
			setCenterY(CIRCLE_DIAMETER/2);
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {


	}
}
