/*
 * Copyright (C) 2013 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.glassware;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.mirror.Mirror;
import com.google.api.services.mirror.model.Location;
import com.google.api.services.mirror.model.MenuItem;
import com.google.api.services.mirror.model.MenuValue;
import com.google.api.services.mirror.model.Notification;
import com.google.api.services.mirror.model.NotificationConfig;
import com.google.api.services.mirror.model.TimelineItem;
import com.google.api.services.mirror.model.UserAction;
import com.google.common.collect.Lists;

import glasswaredata.FaceBook;
import glasswaredata.Twitter;
import glasswaredata.LinkedIn;
import glasswareserviceimpl.FaceBookServiceImpl;
import glasswareserviceimpl.LinkedInServiceImpl;
import glasswareserviceimpl.TwitterServiceImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.logging.Logger;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handles the notifications sent back from subscriptions
 *
 * @author Jenny Murphy - http://google.com/+JennyMurphy
 */
public class NotifyServlet extends HttpServlet {
  private static final Logger LOG = Logger.getLogger(NotifyServlet.class.getSimpleName());
  private static final FaceBookServiceImpl faceBookService = new FaceBookServiceImpl();
  private static final LinkedInServiceImpl linkedInService = new LinkedInServiceImpl();
  private static final TwitterServiceImpl twitterService = new TwitterServiceImpl();

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // Respond with OK and status 200 in a timely fashion to prevent redelivery
    response.setContentType("text/html");
    Writer writer = response.getWriter();
    writer.append("OK");
    writer.close();

    // Get the notification object from the request body (into a string so we
    // can log it)
    BufferedReader notificationReader =
        new BufferedReader(new InputStreamReader(request.getInputStream()));
    String notificationString = "";

    // Count the lines as a very basic way to prevent Denial of Service attacks
    int lines = 0;
    while (notificationReader.ready()) {
      notificationString += notificationReader.readLine();
      lines++;

      // No notification would ever be this long. Something is very wrong.
      if (lines > 1000) {
        throw new IOException("Attempted to parse notification payload that was unexpectedly long.");
      }
    }

    LOG.info("got raw notification " + notificationString);

    JsonFactory jsonFactory = new JacksonFactory();

    // If logging the payload is not as important, use
    // jacksonFactory.fromInputStream instead.
    Notification notification = jsonFactory.fromString(notificationString, Notification.class);

    LOG.info("Got a notification with ID: " + notification.getItemId());

    // Figure out the impacted user and get their credentials for API calls
    String userId = notification.getUserToken();
    Credential credential = AuthUtil.getCredential(userId);
    Mirror mirrorClient = MirrorClient.getMirror(credential);


    if (notification.getCollection().equals("locations")) {
      LOG.info("Notification of updated location");
      Mirror glass = MirrorClient.getMirror(credential);
      // item id is usually 'latest'
      Location location = glass.locations().get(notification.getItemId()).execute();

      LOG.info("New location is " + location.getLatitude() + ", " + location.getLongitude());
      MirrorClient.insertTimelineItem(
          credential,
          new TimelineItem()
              .setText("GlassFriends says you are now at " + location.getLatitude()
                  + " by " + location.getLongitude())
              .setNotification(new NotificationConfig().setLevel("DEFAULT")).setLocation(location)
              .setMenuItems(Lists.newArrayList(new MenuItem().setAction("NAVIGATE"))));

      // This is a location notification. Ping the device with a timeline item
      // telling them where they are.
    } else if (notification.getCollection().equals("timeline")) {
      // Get the impacted timeline item
      TimelineItem timelineItem = mirrorClient.timeline().get(notification.getItemId()).execute();
      LOG.info("Notification impacted timeline item with ID: " + timelineItem.getId());

      // If it was a share, and contains a photo, update the photo's caption to
      // acknowledge that we got it.
      if (notification.getUserActions().contains(new UserAction().setType("SHARE"))
          && timelineItem.getAttachments() != null && timelineItem.getAttachments().size() > 0) {
        LOG.info("It was a share of a photo. Updating the caption on the photo.");

        String caption = timelineItem.getText();
        if (caption == null) {
          caption = "";
        }

        // Create a new item with just the values that we want to patch.
        TimelineItem itemPatch = new TimelineItem();
        itemPatch.setText("GlassFriends got your photo! " + caption);

        // Patch the item. Notice that since we retrieved the entire item above
        // in order to access the caption, we could have just changed the text
        // in place and used the update method, but we wanted to illustrate the
        // patch method here.
        mirrorClient.timeline().patch(notification.getItemId(), itemPatch).execute();
      } else if (notification.getUserActions().contains(new UserAction().setType("LAUNCH"))) {
        LOG.info("It was a note taken with the 'take a note' voice command. Processing it.");

        // Grab the spoken text from the timeline card and update the card with
        // an HTML response (deleting the text as well).
        String noteText = timelineItem.getText();

        timelineItem.setText(null);
        //MAKE CARDS HERE FROM noteText
        
        //FACEBOOK
        FaceBook fbuser = (FaceBook) faceBookService.getInfo(noteText);
        //fbuser is now filled with correct stuff
        
        //LINKEDIN
        LinkedIn liuser = (LinkedIn) linkedInService.getInfo(noteText);
        //liuser now filled with correct stuff
        
        Twitter twuser = (Twitter) twitterService.getInfo(noteText);
        
        timelineItem.setHtml(makeHtmlForCard("<p class='text-auto-size'>"
            + "Found results for <em class='red'>" + noteText + "</em> on FB / LinkedIn / Twitter! " + "</p>"));
        timelineItem.setMenuItems(Lists.newArrayList(
            new MenuItem().setAction("DISMISS")));
        timelineItem.setBundleId("12345-"+noteText);
        timelineItem.setIsBundleCover(true);

        
        MenuItem inspectPerson = new MenuItem().setAction("CUSTOM").setValues(Lists.newArrayList(
        															new MenuValue().setDisplayName("More Info")));
        
        ArrayList<MenuItem> personMenu = Lists.newArrayList();
        personMenu.add(inspectPerson);
        personMenu.add(new MenuItem().setAction("DISMISS"));
        
        TimelineItem timelineItem1 = new TimelineItem();
        timelineItem1.setTitle("Page 1");
        timelineItem1.setHtml(makeHtmlForCard("<p class='text-auto-size'>"
                + "<article>"+
  "<figure><img src='"+fbuser.getImg_src()+"'>  </figure>  <section><table class='text-small align-justify'>"+
  "<tbody><tr><td>Service</td><td>Facebook</td></tr><tr><td>Name</td><td>"+ fbuser.getName() +"</td> </tr><tr><td>About</td><td>" +
  fbuser.getAbout()+"</td></tr></tbody></table></section></article>" + "</p>"));
        timelineItem1.setBundleId("12345-"+noteText);
        timelineItem1.setMenuItems(personMenu);

        TimelineItem timelineItem2 = new TimelineItem();
        timelineItem2.setTitle("Page 2");
        timelineItem2.setHtml(makeHtmlForCard("<p class='text-auto-size'>"
        		+ "<article>"+
        		  "<figure><img src='https://mirror-api-playground.appspot.com/links/lincoln.png'>  </figure>  <section><table class='text-small align-justify'>"+
        		  "<tbody><tr><td>Service</td><td>Twitter</td></tr><tr><td>Name</td><td>"+ noteText +"</td> </tr><tr><td>About</td><td>" +
        		  twuser.getAbout()+"</td></tr></tbody></table></section></article>"+ "</p>"));
        timelineItem2.setBundleId("12345-"+noteText);
        timelineItem2.setMenuItems(personMenu);

        
        mirrorClient.timeline().insert(timelineItem2).execute();
        mirrorClient.timeline().insert(timelineItem1).execute();
        mirrorClient.timeline().update(timelineItem.getId(), timelineItem).execute();
        
        LOG.info("Sent the response with results.");
        
      }  else if (notification.getUserActions().contains(new UserAction().setType("CUSTOM").setPayload(""))){
    	  LOG.info("CUSTOM:::::: User has requested more info for bundle: " + timelineItem.getBundleId() + " specifically, "+
    			  			timelineItem.getTitle());
    	  
      }else {
        LOG.warning("I don't know what to do with this notification, so I'm ignoring it.");
      }
    }
  }

  /**
   * Wraps some HTML content in article/section tags and adds a footer
   * identifying the card as originating from the GlassFriends.
   *
   * @param content the HTML content to wrap
   * @return the wrapped HTML content
   */
  private static String makeHtmlForCard(String content) {
    return "<article class='auto-paginate'>" + content
        + "<footer><p>GlassFriends</p></footer></article>";
  }
}
