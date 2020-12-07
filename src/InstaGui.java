//Authors:	1. Akrith H Nayak
//		 	2. Akash Shetty


//importing required libraries
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONObject;
import org.json.JSONArray;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import javax.imageio.ImageIO;
import javax.swing.*;



public class InstaGui extends JFrame {					// main class InstaGui.
	 
	public CardLayout cardLayout;						// card layout to switch between panels.
	public JPanel mainPanel;							// main panel.
	public UsernamePanel username_panel;				// username panel displayed in the beginning.
	public DetailsPanel details_panel;					// details panel pops when 'Details' button is clicked in username panel.
	public PostDownloadPanel pos_down_panel;			// panel to download posts. Visible only for public instagram accounts.
	public String img_url, username;					
	public boolean exit, hasnext;
	public String  query_id, id, dir_path;
    public JSONObject  edges;
    public int post_count, start, end, post_filled;		// variables to keep track of 'posts_array'.
    public String[] posts_array;


	public InstaGui() {
		
		super("Insta-Pic-Downloader");											// setting title.
		dir_path = "C:\\Users\\AkrithHNayak\\Pictures\\insta_pro_pics\\";		// path where pictures to be stored.
		cardLayout = new CardLayout();											// creating cardlayout object.
		mainPanel = new JPanel(cardLayout);										// passing cardlayout object to 'mainPanel' to we can switch required layouts to mainaPanel.
		
		username_panel = new UsernamePanel();     							    // creating username panel (first UI).
		details_panel = new DetailsPanel();										// creating details panel (second UI).
		pos_down_panel = new PostDownloadPanel();								// creating post download UI.
		
		mainPanel.add(username_panel, "username");								//	adding all the panels(UI) to mainPanel
		mainPanel.add(details_panel, "details");								//  so that we can switch between panels.
		mainPanel.add(pos_down_panel, "post_down");
		
		add(mainPanel);															// adding mainPanel to JFrame.
	
		DetailsHandler details_handler = new DetailsHandler();					// creating a handler for 'Details' button in username panel.
		username_panel.details.addActionListener(details_handler);
		
		details_panel.download.addActionListener(new ActionListener() {			// handler for 'Download' button in second frame.
			
			@Override
			public void actionPerformed(ActionEvent e) {
				SaveImage(img_url);												// call 'SaveImage' method to download the pofile pic.
				
			}
		});
		
			
		BackHomeHandler home_handler = new BackHomeHandler();					// handler for 'Back' in details panel (2nd UI) and
		details_panel.back.addActionListener(home_handler);						// 'Home' in post download panel (3rd UI)
		pos_down_panel.home.addActionListener(home_handler);					// On clicking, navigates to home page i.e, username panel (1st UI).
		
		pos_down_panel.back.addActionListener(new ActionListener() {			// handler for 'Back' in post download panel (3rd UI).
			
			@Override
			public void actionPerformed(ActionEvent e) {

				pos_down_panel.field.setText("");								// reseting text in labels in post download panel (3rd UI).
				cardLayout.show(mainPanel, "details");							// switching from post download panel to details panel (3rd UI -> 2nd UI)
				
			}
		});
		
		pos_down_panel.loadMore.addActionListener(new ActionListener() {							// loading more images. (instagram allows only to scrape 12 posts at a time,
																									// so we need to load more posts if there are more than 12 posts).
			@SuppressWarnings("resource")															// Note: This button is placed in post download panel, so the button will be only visible 
			@Override																				// if the account is public.
			public void actionPerformed(ActionEvent e) {
				
				try {
					if(end >= post_count) return;
					post_filled = end;																// 'post_filled' keeps track of number of pics loaded in the frame
					System.out.println(post_filled);												// prints 'post_filled' in console only if the 'LoadMore' button is pressed.
																									
				    									// creating an array to hold url of images to be displayed in post download panel (3rd UI).
				    
				    int counter = start;	
				    String json_string = edges.toString();
				    String key = json_string.substring(json_string.indexOf("\"")+1, json_string.indexOf("\"", json_string.indexOf("\"")+1));
				    JSONArray post_array = edges.getJSONObject(key).getJSONArray("edges");		// scraping the json object.
				    String display_array[] = new String[post_array.length()];
				    
				    
				    
				    for(int k=0; k<post_array.length(); k++) {
				    	String post_link = post_array.getJSONObject(k).getJSONObject("node").getString("display_url"); // url of post to be downloaded.
				    	String dis_link = post_array.getJSONObject(k).getJSONObject("node").getString("thumbnail_src");	// url of post to be displayed.
				    	posts_array[counter] = post_link;											// appending downloading post link to 'post_array'.
				    	display_array[k] = dis_link;												// appending displaying post link to 'display_array'.
				    	counter++;
				    }
				    
				    DisplayImage(display_array);													// calling 'DisplayImage' to display appended image urls to 'display_array'.
				    end += post_array.length();
				    if (post_count < end) {														// updating the values of end.
				    	hasnext = false;
				    }
				    if (hasnext) {																	// parsing the next page.
					    JSONObject page_info = edges.getJSONObject(key).getJSONObject("page_info");
					    String end_cursor = page_info.getString("end_cursor");
					    end_cursor = end_cursor.substring(0, end_cursor.length()-2);
					    String json_url = "https://www.instagram.com/graphql/query/?query_hash="+query_id+"&variables=%7B%22id%22%3A%22"+id+"%22%2C%22first%22%3A12%2C%22after%22%3A%22"+end_cursor+"%3D%3D%22%7D";
						String json_content = new Scanner(new URL(json_url).openStream(), "UTF-8").useDelimiter("\\A").next();
					    JSONObject jsonfile = new JSONObject(json_content);
					    edges = jsonfile.getJSONObject("data").getJSONObject("user");
				    }
				    

				    
				     
				} catch (Exception e2) {
					e2.printStackTrace();
				}		
				
			}
		});
		
		details_panel.download_posts.addActionListener(new ActionListener() {					    // handler for 'Download Posts' in details panel (2nd UI).
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				cardLayout.show(mainPanel, "post_down");											// switching from 2nd UI to 3rd UI.
				
			}
		});
		
		PostDownloadHandler post_handler = new PostDownloadHandler();								// handler for 'Download' button in 3rd UI.
		pos_down_panel.download.addActionListener(post_handler);
		
		
	}
	
	
	public void SaveImage(String img_url) {															// method to download image.
		
		try {
			
			URL url = new URL(img_url);																// converting 'img_url' from String to URL object.
			BufferedImage img = ImageIO.read(url);													// creating the buffer image from URL object.
			File file = new File(dir_path+username+"_prof_pic.png");								// creating the file for image, naming by username
			ImageIO.write(img, "png", file);														// and writing buffer image to the file.
			
		}
		catch (Exception e) {
			;
		}
		
	}
	
	public void SavePost(String img_url, String post) {												// download post method.
		
		try {
			
			URL url = new URL(img_url);																// converting 'img_url' from String to URL object.
			BufferedImage img = ImageIO.read(url);													// creating the buffer image from URL object.
			File file = new File(dir_path+username+"\\"+post+".png");								// creating the file for image, naming by post number
			ImageIO.write(img, "png", file);														// and writing buffer image to the file.
			
		}
		catch (Exception e) {
			;
		}
		
	}
	
	public void DisplayImage(String[] nor_url) {													// method to display image in 3rd UI.
		
		new Thread() {
			
			public void run() {
				for(String url_: nor_url) {
					pos_down_panel.generate_labels(url_);											// calling the 'generate_labels' method in 'PostDownloadPanel' class.
				}
			}
		}.start();
		
	}
	
	
	
	public class DetailsHandler implements ActionListener {											// handler for 'Details' button in username panel.
		
		@SuppressWarnings("resource")
		public void actionPerformed(ActionEvent event) {
			
			Document doc = null;
			String user_name = username_panel.field.getText();
			username = user_name;
			start = 0;
			hasnext = true;
			post_filled = 0;
			pos_down_panel.i = 0;
			try {
					
				doc = Jsoup.connect("https://www.instagram.com/"+username).get();					// parsing html document of the requested page.
				Element script_tag = doc.select("script[type=\"text/javascript\"]").get(3);			// selecting the 3rd javascript tag from parsed html document.
				String script_contents = script_tag.html();
				script_contents = script_contents.substring(21, script_contents.length()-1);
				
				JSONObject json = new JSONObject(script_contents);								  	// creating JSON object of the string.
			    edges = json.getJSONObject("entry_data").getJSONArray("ProfilePage").getJSONObject(0).getJSONObject("graphql").getJSONObject("user");
			    System.out.println(edges);
			    String profile_pic_hd = edges.getString("profile_pic_url_hd");									// scraping profile pic url to download.
			    String nor_img_url = edges.getString("profile_pic_url");										// scraping profile pic url to display.
			    String followers = edges.getJSONObject("edge_followed_by").getString("count");					// scraping the follower count.
			    String following = edges.getJSONObject("edge_follow").getString("count");						// scraping the following count.
			    String name = edges.getString("full_name");														// scraping the name.
			    String is_private = edges.getString("is_private");												// checking if the account is private.
			    String posts = edges.getJSONObject("edge_owner_to_timeline_media").getString("count");			// scraping the post count.
			    
			    
			    post_count = Integer.parseInt(posts);
			    
			    if (!(is_private.equals("true"))) {																// if the account is public start displaying the posts in 3rd UI.
				    String query= doc.select("script[src*=/static/bundles/es6/Consumer.js]").get(0).attr("src");
				    System.out.println("https://www.instagram.com"+query);
				    String key = new Scanner(new URL("https://www.instagram.com"+query).openStream(), "UTF-8").useDelimiter("\\A").next();
				    query_id = key.substring(key.indexOf("||void 0===l?void 0:l.pagination},queryId:\"")+43, key.indexOf("\",queryParams:t=>({id:t})", key.indexOf("||void 0===l?void 0:l.pagination},queryId:\"")+42)); 
//				    query_id = "72fb0a35705033d45d39ffe3d231520a";												// scraping query id to request the next 12 posts.
				    id = edges.getString("id");											// as instagram allows only 12 posts to be parsed in one request
																						// we need to request next 12 posts if its available.
				    System.out.println(query_id);
				    																	
				    
				    
				    posts_array = new String[post_count];								// creating posts array to hold all the post urls.
				    
				    
				    
				    JSONArray post_array = edges.getJSONObject("edge_owner_to_timeline_media").getJSONArray("edges");					// scraping the json object.
				    String display_array[] = new String[post_array.length()];
				    if (post_count< 12)
				    	end = post_count;
				    else 
				    	end = post_array.length();
				    
				    for(int i=start; i<end; i++) {
				    	
				    	String post_link = post_array.getJSONObject(i).getJSONObject("node").getJSONArray("thumbnail_resources").getJSONObject(4).getString("src");	// url of post to be downloaded.
				    	String dis_link = post_array.getJSONObject(i).getJSONObject("node").getJSONArray("thumbnail_resources").getJSONObject(0).getString("src");	// url of post to be displayed.
				    	posts_array[i] = post_link;										// appending downloading post link to 'post_array'.
				    	display_array[i] = dis_link;									// appending downloading post link to 'display_array'.
				    	
				    }
				    
				    if (post_count <= 12) 												// updating the values of start and end.
				    	hasnext = false;
				    
				    DisplayImage(display_array);										// calling 'DisplayImage' to display appended image urls to 'display_array'
				    
				    if(hasnext) {														// parsing the next page.
				    	
					    JSONObject page_info = edges.getJSONObject("edge_owner_to_timeline_media").getJSONObject("page_info");
					    String end_cursor = page_info.getString("end_cursor");
					    end_cursor = end_cursor.substring(0, end_cursor.length()-2);
					    String json_url = "https://www.instagram.com/graphql/query/?query_hash="+query_id+"&variables=%7B%22id%22%3A%22"+id+"%22%2C%22first%22%3A12%2C%22after%22%3A%22"+end_cursor+"%3D%3D%22%7D";
						String json_content = new Scanner(new URL(json_url).openStream(), "UTF-8").useDelimiter("\\A").next();
						System.out.println(json_content);
					    JSONObject jsonfile = new JSONObject(json_content);
					    System.out.println(jsonfile);
					    edges = jsonfile.getJSONObject("data").getJSONObject("user");
					    System.out.println(edges);
				    }
			    }
			    details_panel.DisplayImage(nor_img_url);															// Displaying profile pisc in 2nd UI.
				details_panel.update_details(details_panel, followers, following, posts, name, is_private);			// updating details in 2nd UI.
				
			    img_url = profile_pic_hd;
				

			}
			catch (Exception e) {
				e.printStackTrace();
			}
			
			cardLayout.show(mainPanel, "details");								// switching from 1st UI to 2nd UI.
			
		}
		

		
	}
	
	
	public class PostDownloadHandler implements ActionListener {				// handler for 'DownloadPosts' in 3rd UI.
		
		public void actionPerformed(ActionEvent event) {
			
			try {
				File directory = new File(dir_path+username);					// creating file object.
				
				if (! directory.exists()){										// checking if a directory exists in that path
			        directory.mkdir();											// else create a directory.
				}
				
				String post_no = pos_down_panel.field.getText();				// get the post numbers to download.
				String[] post_numbers = post_no.split(",");						// split the string and store it in a array.
				
				for(String post: post_numbers) {
					
					SavePost(posts_array[Integer.parseInt(post)-1], post);		// calling method to download posts.
				}
			} catch (Exception e) {
				;
			}
			
		}
		
	}
	
	
	public class BackHomeHandler implements ActionListener {					// handler for 'Back' in details panel (2nd UI) and
																				// 'Home' in post download panel (3rd UI)
																				// On clicking, navigates to home page i.e, username panel (1st UI).	
		public void actionPerformed(ActionEvent e) {
			
			username_panel.field.setText("");									// restting all the components in 2nd UI.
			details_panel.name_label.setText("Name : ");
			details_panel.followers_label.setText("Followers : ");
			details_panel.following_label.setText("Following : ");
			details_panel.posts_label.setText("Posts : ");
			details_panel.image_label.setIcon(new ImageIcon("No Image"));
			pos_down_panel.field.setText("");
			pos_down_panel.post_panel.removeAll();
			pos_down_panel.post_panel.revalidate();
			pos_down_panel.post_panel.repaint();
			cardLayout.show(mainPanel, "username");								// switching from 3rd UI or 2nd UI to 1st UI.
			
		}
	}
}


class UsernamePanel extends JPanel {											// 1st UI, i.e username panel.
	
	JTextField field;
	JButton details;
	JLabel header_label_1;
	JLabel header_label_2;
	JLabel user_label;
	
	public UsernamePanel() {

		setLayout(null);
		setBackground(Color.BLACK);
		
		user_label = new JLabel("Username:");									// label of username.
		user_label.setBounds(60, 200, 120, 30);
		user_label.setBackground(Color.yellow);
		user_label.setForeground(Color.red);
		user_label.setFont(new Font("Courier", Font.BOLD, 20));
		add(user_label);
		
		field = new JTextField(20);												// field to enter username.
		field.setBounds(180, 200, 250, 30);
		field.setFont(new Font("Courier", Font.BOLD, 20));
		field.setForeground(Color.blue);
		field.setBorder(javax.swing.BorderFactory.createEmptyBorder());
		add(field);
		
		header_label_1 = new JLabel("Profile Pic");								// heading label 1.
		header_label_1.setBounds(180, 50, 240, 25);
		header_label_1.setFont(new Font("Courier", Font.BOLD, 36));
		header_label_1.setForeground(Color.blue);
        add(header_label_1);
        
        header_label_2 = new JLabel("Downloader");								// heading label 2.
        header_label_2.setBounds(190, 100, 240, 25);
        header_label_2.setFont(new Font("Courier", Font.BOLD, 36));
        header_label_2.setForeground(Color.blue);
        add(header_label_2);
		
		details = new JButton("Details");										// 'Details' button.
		details.setFont(new Font("Courier", Font.BOLD, 20));
		details.setBackground(Color.blue);
		details.setForeground(Color.magenta);
		details.setBounds(240, 250, 120, 30);
		add(details);
		
	}
		
}

class DetailsPanel extends JPanel {												// 2nd UI, i.e details panel.

	JLabel label, image_label, name_label, followers_label, following_label, posts_label;
	JButton download, back, download_posts;
	
	
	public DetailsPanel() {
    	
    	setLayout(null);
		setBackground(Color.black);
		
        image_label = new JLabel();												// image label to display image.
		image_label.setBounds(170, 30, 350, 170);
		add(image_label);
		
		download = new JButton("Download");										// 'Download' button to download profile pic.
		download.setFont(new Font("Courier", Font.BOLD, 20));
		download.setBackground(Color.yellow);
		download.setForeground(Color.black);
		download.setBounds(175, 200, 140, 30);
		add(download);
		
		download_posts = new JButton("Download posts");							// 'Download Posts' button to switch to 3rd UI.
		download_posts.setFont(new Font("Courier", Font.BOLD, 15));
		download_posts.setBackground(Color.red);
		download_posts.setForeground(Color.yellow);
		download_posts.setBounds(400, 130, 170, 30);
		add(download_posts);
		
		back = new JButton("Back");												// 'Back' button to switch to 1st UI.
		back.setFont(new Font("Courier", Font.BOLD, 10));
		back.setBackground(Color.red);
		back.setForeground(Color.yellow);
		back.setBounds(10, 15, 60, 20);
		add(back);
		
		name_label = new JLabel("Name : ");										// label to display name.
		name_label.setBounds(20, 250, 1000, 30);
		name_label.setBackground(Color.yellow);
		name_label.setForeground(Color.red);
		name_label.setFont(new Font("Courier", Font.BOLD, 25));
		add(name_label);
		
		followers_label = new JLabel("Followers : ");							// label to display followers count.
		followers_label.setBounds(20, 300, 500, 30);
		followers_label.setBackground(Color.yellow);
		followers_label.setForeground(Color.red);
		followers_label.setFont(new Font("Courier", Font.BOLD, 25));
		add(followers_label);
		
		following_label = new JLabel("Following : ");							// label to display following count
		following_label.setBounds(20, 350, 500, 30);
		following_label.setBackground(Color.yellow);
		following_label.setForeground(Color.red);
		following_label.setFont(new Font("Courier", Font.BOLD, 25));
		add(following_label);
		
		posts_label = new JLabel("Posts : ");									// label to display post count.
		posts_label.setBounds(20, 400, 500, 30);
		posts_label.setBackground(Color.yellow);
		posts_label.setForeground(Color.red);
		posts_label.setFont(new Font("Courier", Font.BOLD, 25));
        add(posts_label);
        
    }


	public void update_details(DetailsPanel obj, String follower_c, String following_c, String posts_c, String name, String is_private) {
		
		name_label.setText("Name : "+name);
		followers_label.setText("Followers : "+follower_c);
		following_label.setText("Following : "+following_c);					// method to update detais in 2nd UI.
		posts_label.setText("Posts : "+posts_c);
		if(is_private.equals("true")) 		
			download_posts.setEnabled(false);
		else
			download_posts.setEnabled(true);
	
	}
    
    
    public void DisplayImage(String img_url) {									// method to display image in 2nd UI.

    	try {
    		
    		URL url = new URL(img_url);											// converting 'img_url' from String to URL object.
    		BufferedImage img = ImageIO.read(url);								// creating the buffer image from URL object.
    		image_label.setIcon(new ImageIcon(img));							// setting image icon into label in 2nd UI.
    		
        }
    	catch (Exception e) {
    		
    		;
    		
    	}
		
	}

}

class PostDownloadPanel extends JPanel {										// 3rd UI, i.e post download panel.
	
	
	JTextField field;
	JButton download, back, home, loadMore;
	JLabel post_label, note, posts;
	JScrollPane posts_dis;
	JPanel post_panel, row_panel;
	int i;
	
	public PostDownloadPanel() {
		
		setLayout(null);
		setBackground(Color.BLACK);
		
		back = new JButton("Back");												// 'Back' button to switch to 2nd UI.
		back.setFont(new Font("Courier", Font.BOLD, 10));
		back.setBackground(Color.red);
		back.setForeground(Color.yellow);
		back.setBounds(10, 15, 60, 20);
		add(back);
		
		home = new JButton("Home");												// 'Home' button to switch to 1st UI.
		home.setFont(new Font("Courier", Font.BOLD, 10));
		home.setBackground(Color.red);
		home.setForeground(Color.yellow);
		home.setBounds(10, 40, 60, 20);
		add(home);
		
		posts = new JLabel("Posts");											// posts label.
		posts.setBounds(280, 5, 60, 30);
		posts.setForeground(Color.yellow);
		posts.setFont(new Font("Courier", Font.BOLD, 20));
		add(posts);
		
		loadMore = new JButton("LoadMore");										// load more button to load more posts.
		loadMore.setBounds(0, 335, 90, 50);
		loadMore.setForeground(Color.cyan);
		loadMore.setBackground(Color.black);
		loadMore.setFont(new Font("Courier", Font.BOLD, 10));
		loadMore.setBorder(javax.swing.BorderFactory.createEmptyBorder());
		add(loadMore);
		
		post_panel = new JPanel();												// panel to insert components one below another.
		post_panel.setLayout(new BoxLayout(post_panel, BoxLayout.Y_AXIS));		// achieved by BoxLayout.Y_AXIS policy.

		posts_dis = new JScrollPane(post_panel);								// JScrollPane to display posts.
		posts_dis.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		posts_dis.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		posts_dis.setBounds(90, 40, 490, 345);
		add(posts_dis);
		
		post_label = new JLabel("Enter the number of posts:");					// label.
		post_label.setBounds(0, 390, 350, 30);
		post_label.setForeground(Color.red);
		post_label.setFont(new Font("Courier", Font.BOLD, 20));
		add(post_label);
		
		field = new JTextField(20);												// get the post numbers to download.
		field.setBounds(320, 395, 250, 20);
		field.setFont(new Font("Courier", Font.BOLD, 20));
		field.setForeground(Color.blue);
		field.setBorder(javax.swing.BorderFactory.createEmptyBorder());
		add(field);
			
		download = new JButton("Download");										// button to download posts.
		download.setFont(new Font("Courier", Font.BOLD, 20));
		download.setBackground(Color.blue);
		download.setForeground(Color.magenta);
		download.setBounds(430, 430, 130, 30);
		add(download);
		
		note = new JLabel("Note : Enter the post number seperated by commas(,) Eg. 1,2,3 ");
		note.setBounds(0, 430, 500, 25);
		note.setForeground(Color.yellow);										// note label.
		note.setFont(new Font("Courier", Font.BOLD, 10));
		add(note);
		
	}

	
	public void generate_labels(String img_url) {								// method to display posts in JScrollPane.
		
		JLabel image = new JLabel();
		
		if (i%3 == 0) {															// do the following for every 3 images.
			
			row_panel = new JPanel();											// each 'row_panel' contans three posts.
			row_panel.setLayout(new GridLayout(0, 3));
			post_panel.add(row_panel);											// adding 'row_panel' to 'post_panel'.
			JLabel one, two, three;
			one = new JLabel(Integer.toString(i+1), JLabel.CENTER);				// three labels to number the posts.
			two = new JLabel(Integer.toString(i+2), JLabel.CENTER);				// as we display posts in three column we need
			three = new JLabel(Integer.toString(i+3), JLabel.CENTER);			// three labels to store the post number.
			JPanel num_panel = new JPanel();
			num_panel.setLayout(new GridLayout(0, 3));
			num_panel.add(one);													// adding three post number labels to
			num_panel.add(two);													// num_panel label.
			num_panel.add(three);
			post_panel.add(num_panel);											// adding num_panel to post_panel.
			
		}
		try {
    		
    		URL url = new URL(img_url);											// converting 'img_url' from String to URL object.
    		BufferedImage img = ImageIO.read(url);								// creating the buffer image from URL object.
    		
    		
    		Image tmp = img.getScaledInstance(150, 150, Image.SCALE_SMOOTH);	// resizing image to 150x150 so that it fits perfectly in the label.
    	    BufferedImage dimg = new BufferedImage(150, 150, BufferedImage.TYPE_INT_ARGB);
    	    Graphics2D g2d = dimg.createGraphics();
    	    g2d.drawImage(tmp, 0, 0, null);
    	    g2d.dispose();
    		image.setIcon(new ImageIcon(dimg));									// resulting image after resizing is stored in 'dimg'.
    		i++;
    		
        }
    	catch (Exception e) {
    		
    		;
    		
    	}
		row_panel.add(image);													// adding image to 'row_panel'.

	}
	
}
