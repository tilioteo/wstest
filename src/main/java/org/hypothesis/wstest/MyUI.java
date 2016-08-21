package org.hypothesis.wstest;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.annotation.WebServlet;

import org.vaadin.websocket.ui.WebSocket;
import org.vaadin.websocket.ui.WebSocket.CloseEvent;
import org.vaadin.websocket.ui.WebSocket.CloseListener;
import org.vaadin.websocket.ui.WebSocket.ErrorListener;
import org.vaadin.websocket.ui.WebSocket.FailEvent;
import org.vaadin.websocket.ui.WebSocket.FailListener;
import org.vaadin.websocket.ui.WebSocket.MessageEvent;
import org.vaadin.websocket.ui.WebSocket.MessageListener;
import org.vaadin.websocket.ui.WebSocket.OpenEvent;
import org.vaadin.websocket.ui.WebSocket.OpenListener;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 * This UI is the application entry point. A UI may either represent a browser
 * window (or tab) or some part of a html page where a Vaadin application is
 * embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is
 * intended to be overridden to add component to the user interface and
 * initialize non-component functionality.
 */
@SuppressWarnings("serial")
@Theme("mytheme")
@Widgetset("org.hypothesis.wstest.MyAppWidgetset")
public class MyUI extends UI {

	private WebSocket webSocket;
	private Label rich;
	private String log = "";
	private Panel panel;

	private void addLog(String text) {
		Date now = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
		log += "<i>" + format.format(now) + "</i> ";
		log += text;
		rich.setValue(log);

		panel.setScrollTop(1000000);
	}

	private void clearLog() {
		log = "";
		rich.setValue(log);
	}

	@Override
	protected void init(VaadinRequest vaadinRequest) {
		final VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();

		final HorizontalLayout hlayout = new HorizontalLayout();
		hlayout.setSizeFull();

		final VerticalLayout vlayout1 = new VerticalLayout();
		final VerticalLayout vlayout2 = new VerticalLayout();
		vlayout2.setSizeFull();

		rich = new Label();
		rich.setContentMode(ContentMode.HTML);
		rich.setSizeUndefined();
		rich.setWidth(100f, Unit.PERCENTAGE);

		final TextField urlText = new TextField();
		urlText.setIcon(FontAwesome.GLOBE);
		urlText.setCaption("Type websocket url to connect:");
		urlText.setInputPrompt("ws://localhost:12345");
		urlText.setWidth(300, Unit.PIXELS);

		final Button connectButton = new Button("Connect");
		connectButton.setIcon(FontAwesome.BOLT);
		connectButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				webSocket.setUrl(urlText.getValue());
			}
		});
		connectButton.setStyleName(ValoTheme.BUTTON_FRIENDLY);

		final Button disconnectButton = new Button("Disconnect");
		disconnectButton.setEnabled(false);
		disconnectButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				webSocket.setUrl(null);
			}
		});
		disconnectButton.setStyleName(ValoTheme.BUTTON_DANGER);

		final CssLayout cssLayout = new CssLayout(connectButton, disconnectButton);
		cssLayout.setStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

		final TextArea messageText = new TextArea();
		messageText.setWidth(300, Unit.PIXELS);
		messageText.setEnabled(false);
		messageText.setCaption("Type message to send:");

		final Button sendButton = new Button("Send");
		sendButton.setIcon(FontAwesome.SEND);
		sendButton.setEnabled(false);
		sendButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				if (!messageText.getValue().trim().isEmpty()) {
					addLog("<font color=\"#3333ff\"><strong>message sent:</strong></font>" + messageText.getValue()
							+ "<br/>");
					webSocket.send(messageText.getValue());
				}
			}
		});
		sendButton.setStyleName(ValoTheme.BUTTON_PRIMARY);
		sendButton.addStyleName(ValoTheme.BUTTON_HUGE);

		CssLayout cssLayout2 = new CssLayout();
		cssLayout2.setIcon(FontAwesome.LIST);
		cssLayout2.setCaption("Log view");
		cssLayout2.addStyleName(ValoTheme.LAYOUT_CARD);
		cssLayout2.setSizeFull();
		panel = new Panel();
		panel.addStyleName(ValoTheme.PANEL_BORDERLESS);
		panel.setSizeFull();
		VerticalLayout vlayout3 = new VerticalLayout(rich);
		// vlayout3.setSizeFull();
		vlayout3.setMargin(true);
		panel.setContent(vlayout3);
		cssLayout2.addComponent(panel);

		final Button clearButton = new Button("Clear");
		clearButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				clearLog();
			}
		});

		webSocket = new WebSocket();

		webSocket.addOpenListener(new OpenListener() {
			@Override
			public void open(OpenEvent event) {
				addLog("<font color=\"#00cc00\"><strong>connected:</strong></font>" + urlText.getValue() + "<br/>");
				// Notification.show("connected");
				urlText.setEnabled(false);
				connectButton.setEnabled(false);
				disconnectButton.setEnabled(true);
				messageText.setEnabled(true);
				sendButton.setEnabled(true);
			}
		});

		webSocket.addCloseListener(new CloseListener() {
			@Override
			public void close(CloseEvent event) {
				addLog("<font color=\"#ff0066\"><strong>disconnected:</strong></font>" + urlText.getValue() + "<br/>");
				// Notification.show("disconnected");
				urlText.setEnabled(true);
				connectButton.setEnabled(true);
				disconnectButton.setEnabled(false);
				messageText.setEnabled(false);
				sendButton.setEnabled(false);
			}
		});

		webSocket.addFailListener(new FailListener() {
			@Override
			public void fail(FailEvent event) {
				addLog("<font color=\"#ff6600\"><strong>connection failed:</strong></font>" + urlText.getValue()
						+ "<br/>");
				// Notification.show("connection failed");
			}
		});

		webSocket.addErrorListener(new ErrorListener() {
			@Override
			public void error(org.vaadin.websocket.ui.WebSocket.ErrorEvent event) {
				addLog("<font color=\"red\"><strong>error:</strong>"
						+ (event.getMessage() != null ? event.getMessage() : "") + "</font><br/>");
				// Notification.show("error: " + event.getMessage());
			}
		});

		webSocket.addMessageListener(new MessageListener() {
			@Override
			public void message(MessageEvent event) {
				addLog("<font color=\"#9900ff\"><strong>message recieved:</strong></font>" + event.getMessage()
						+ "<br/>");
				// Notification.show("message: " + event.getMessage());
			}
		});

		vlayout1.addComponents(urlText, cssLayout, messageText, sendButton);
		vlayout1.setWidth(400, Unit.PIXELS);
		vlayout1.setMargin(true);
		vlayout1.setSpacing(true);

		vlayout2.addComponents(cssLayout2, clearButton);
		vlayout2.setExpandRatio(cssLayout2, 1f);
		vlayout2.setMargin(true);
		vlayout2.setSpacing(true);

		hlayout.addComponents(vlayout1, vlayout2);
		hlayout.setExpandRatio(vlayout2, 1f);

		layout.addComponents(webSocket, hlayout);
		layout.setExpandRatio(webSocket, 0f);
		layout.setExpandRatio(hlayout, 1f);
		setContent(layout);
	}

	@WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
	@VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
	public static class MyUIServlet extends VaadinServlet {
	}
}
