package com.controllers;

import com.model.Facade;
import com.model.Ticket;
import com.model.User;
import com.model.Event;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.scene.paint.Color;

import com.event.App;

public class Home implements javafx.fxml.Initializable {

    @FXML
    private ListView<Event> lst_events;

    @FXML
    private Label welcomeLabel;

    @FXML
    private TextField searchField;

    @FXML
    private VBox eventListVBox;

    @FXML
    private Button secondaryButton;

    @FXML
    private HBox categoriesHBox;

    private User currentUser;

    public void setUser(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Welcome, " + user.getFirstName() + "!");
        loadEvents();
        setupCategories();
    }

    private void loadEvents() {
        Facade facade = Facade.getInstance();
        List<Event> events = facade.getAllEvents();

        ObservableList<Event> eventObservableList = FXCollections.observableArrayList(events);

        lst_events.setItems(eventObservableList);

        lst_events.setCellFactory(listView -> new ListCell<Event>() {
            private ImageView imageView = new ImageView();
            private HBox content = new HBox(10);
            private Label textLabel = new Label();
            {
                imageView.setFitHeight(50);
                imageView.setFitWidth(50);
                imageView.setPreserveRatio(true);
                content.getChildren().addAll(imageView, textLabel);
                content.setAlignment(Pos.CENTER_LEFT);
            }

            @Override
            protected void updateItem(Event event, boolean empty) {
                super.updateItem(event, empty);
                if (empty || event == null) {
                    setGraphic(null);
                } else {
                    System.out.println("Path is " + getClass().getResourceAsStream("/com/event/images/" + event.getEventId() + ".jpg"));
                    Image image = new Image(getClass().getResourceAsStream("/com/event/images/" + event.getEventId() + ".jpg"));
                    imageView.setImage(image);

                    textLabel.setText(event.getName() + " - " + event.getDate());
                    setGraphic(content);
                }
            }
        });
    }

    @FXML
    void eventClicked(MouseEvent e) {
        Event event = lst_events.getSelectionModel().getSelectedItem();
        if (event != null) {
            showEventDetailsWindow(event);
        }
    }

    private void showEventDetailsWindow(Event event) {
        Stage detailStage = new Stage(StageStyle.TRANSPARENT);
        detailStage.initOwner(((Stage) lst_events.getScene().getWindow()));

        VBox layout = new VBox();
        layout.getStyleClass().add("event-popup");

        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy");

        Label title = new Label("🎟️ " + event.getName());
        title.getStyleClass().add("event-popup-title");
        
        Label category = new Label("Category: " + event.getCategory());
        Label subCategory = new Label("Subcategory: " + event.getSubCategory());
        Label date = new Label("Date: " + formatter.format(event.getDate()));
        Label capacity = new Label("Capacity: " + event.getCapacity());
        Label ticketsLeft = new Label("Tickets Left: " + event.getTicketsLeft());

        List<Label> infoLabels = List.of(category, subCategory, date, capacity, ticketsLeft);
        infoLabels.forEach(label -> label.getStyleClass().add("event-popup-label"));

        // Create button container
        HBox buttonContainer = new HBox(10);
        buttonContainer.getStyleClass().add("event-popup-button-row");
        
        Button buyBtn = new Button("Buy Ticket");
        buyBtn.getStyleClass().add("btn-primary");
        buyBtn.setOnAction(e -> {
            try {
                System.out.println("=== DEBUG: Buy button clicked ===");
                System.out.println("Event: " + event.getName());
                System.out.println("Current user: " + (currentUser != null ? currentUser.getFirstName() : "USER IS NULL"));
                
                if (currentUser == null) {
                    System.out.println("ERROR: Current user is null - cannot create ticket");
                    detailStage.close();
                    return;
                }
                
                // Create a new ticket for this event
                Ticket newTicket = new Ticket(event, currentUser);
                System.out.println("Created ticket: " + newTicket);
                
                // Initialize tickets list if null
                if (currentUser.getTickets() == null) {
                    currentUser.setTickets(new ArrayList<>());
                    System.out.println("Initialized empty tickets list for user");
                }
                
                // Add the ticket to the user's tickets list
                currentUser.getTickets().add(newTicket);
                
                // Update the facade's current user to ensure persistence
                Facade.getInstance().setCurrentUser(currentUser);
                
                System.out.println("Ticket purchased for: " + event.getName());
                System.out.println("User now has " + currentUser.getTickets().size() + " tickets");
                
                // Show success message
                title.setText("✅ Ticket Purchased!");
                buyBtn.setText("Purchased");
                buyBtn.setDisable(true);
                
                // Auto-close after 2 seconds using Timeline instead of Thread.sleep
                javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(
                        javafx.util.Duration.seconds(2),
                        actionEvent -> detailStage.close()
                    )
                );
                timeline.play();
                
            } catch (Exception ex) {
                System.out.println("ERROR purchasing ticket: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        
        Button closeBtn = new Button("Close");
        closeBtn.getStyleClass().add("close-button");
        closeBtn.setOnAction(e -> detailStage.close());
        
        buttonContainer.getChildren().addAll(buyBtn, closeBtn);
        
        // Add all elements to layout
        layout.getChildren().addAll(title, category, subCategory, date, capacity, ticketsLeft, buttonContainer);
        
        // Create scene and show stage
        Scene scene = new Scene(layout, 400, 300);
        scene.getStylesheets().add(getClass().getResource("/com/event/styles.css").toExternalForm());
        scene.setFill(Color.TRANSPARENT);
        
        detailStage.setScene(scene);
        detailStage.setTitle("Event Details");
        detailStage.setResizable(false);
        detailStage.centerOnScreen();
        detailStage.show();
    }

    private VBox createEventCard(Event event) {
        VBox eventCard = new VBox();
        eventCard.getStyleClass().add("event-card");

        VBox eventImage = new VBox();
        eventImage.getStyleClass().add("event-image");
        eventImage.setPrefHeight(100);

        Label eventEmoji = new Label("🎵");
        eventEmoji.getStyleClass().add("event-emoji");
        eventImage.getChildren().add(eventEmoji);

        VBox eventDetails = new VBox();
        eventDetails.getStyleClass().add("event-details");

        Label eventTitle = new Label(event.getName());
        eventTitle.getStyleClass().add("event-title");

        HBox eventMeta = new HBox();
        eventMeta.getStyleClass().add("event-meta");

        Label eventCategory = new Label("Music");
        eventCategory.getStyleClass().add("event-category");

        Label eventDate = new Label(event.getDate().toString());
        eventDate.getStyleClass().add("event-date");

        eventMeta.getChildren().addAll(eventCategory, eventDate);

        HBox eventCapacity = new HBox();
        eventCapacity.getStyleClass().add("event-capacity");

        Label capacityLabel = new Label("Available: ");
        Label capacityCount = new Label("50 spots");
        capacityCount.getStyleClass().add("capacity-available");

        eventCapacity.getChildren().addAll(capacityLabel, capacityCount);

        Button viewDetailsBtn = new Button("View Details");
        viewDetailsBtn.getStyleClass().add("btn-secondary");
        viewDetailsBtn.setOnAction(e -> showEventDetailsWindow(event));

        eventDetails.getChildren().addAll(eventTitle, eventMeta, eventCapacity, viewDetailsBtn);
        eventCard.getChildren().addAll(eventImage, eventDetails);

        return eventCard;
    }

    private void setupCategories() {
        if (categoriesHBox != null) {
            String[] categories = {"All", "Music", "Sports", "Art", "Food", "Tech"};

            for (String category : categories) {
                Button categoryBtn = new Button(category);
                categoryBtn.getStyleClass().add("category-chip");
                if (category.equals("All")) {
                    categoryBtn.getStyleClass().add("active");
                }
                categoryBtn.setOnAction(this::handleCategorySelection);
                categoriesHBox.getChildren().add(categoryBtn);
            }
        }
    }

    @FXML
    private void handleCategorySelection(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();

        if (categoriesHBox != null) {
            categoriesHBox.getChildren().forEach(node -> {
                if (node instanceof Button) {
                    node.getStyleClass().remove("active");
                }
            });
        }

        if (!clickedButton.getStyleClass().contains("active")) {
            clickedButton.getStyleClass().add("active");
        }

        System.out.println("Category selected: " + clickedButton.getText());
    }

    @FXML
    private void handleViewDetails(ActionEvent event) {
        System.out.println("View Details clicked");
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String searchTerm = searchField.getText();
        System.out.println("Searching for: " + searchTerm);
    }

    @FXML
    private void switchToPrimary(ActionEvent event) {
        try {
            App.setRoot("Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void switchToTickets(MouseEvent event) {
        try {
            App.setRoot("tickets");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void switchToProfile(MouseEvent event) {
        try {
            App.setRoot("profile");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        setUser(Facade.getInstance().getCurrentUser());
    }
}