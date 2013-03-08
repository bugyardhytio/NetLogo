// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mc;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.nlogo.api.CompilerException;
import org.nlogo.api.LogoException;
import org.nlogo.api.ModelingCommonsInterface;
import org.nlogo.nvm.Workspace;
import org.nlogo.swing.MessageDialog;
import scala.Function0;

import javax.swing.JDialog;
import java.awt.Frame;
import java.awt.image.BufferedImage;
import java.util.List;

public class ModelingCommons implements ModelingCommonsInterface {

  public static final String HOST = "http://modelingcommons.org";
  private static HttpClient http = new DefaultHttpClient();
  private Person person;
  private String newUserAgreement;
  private List<String> priorityCountries;
  private List<String> unpriorityCountries;
  private List<Group> groups;
  private Function0<String> saveModel;
  private Function0<BufferedImage> imageFunc;
  private Frame frame;
  private Workspace workspace;

  public ModelingCommons(Function0<String> saveModel, Function0<BufferedImage> imageFunc, Frame frame, Workspace workspace) {
    this.saveModel = saveModel;
    this.imageFunc = imageFunc;
    this.frame     = frame;
    this.workspace = workspace;
  }

  public boolean isLoggedIn() {
    return person != null;
  }

  public List<Group> getGroups() {
    return groups;
  }

  public List<String> getPriorityCountries() {
    return priorityCountries;
  }

  public List<String> getUnpriorityCountries() {
    return unpriorityCountries;
  }

  public String getNewUserAgreement() {
    return newUserAgreement;
  }

  public Person getPerson() {
    return person;
  }

  public String getModelBody() {
    return saveModel.apply();
  }

  //Only login dialog should be able to set this
  void setPerson(Person person) {
    this.person = person;
  }

  public void promptForLogin(final String error) {
    JDialog loginDialog = new LoginDialog(frame, this, error);
    loginDialog.setVisible(true);
  }

  public void promptForLogin() {
    promptForLogin(" ");
  }

  public void promptForUpload(String error) {
    promptForUpload(error, true);
  }

  public void promptForUpload() {
    promptForUpload(" ", true);
  }

  //This is only used for the uploadDialog
  //If an upload fails due to a bad connection, we want to show the upload dialog again
  //But since there is a bad connection, we can't redownload groups
  //Normally you should call promptForUpload(String error) or promptForUpload()
  void promptForUpload(final String error, boolean downloadGroups) {
    if(downloadGroups) {
      Request request = new DownloadGroupsRequest(getHttpClient(), frame) {

        @Override
        protected void onDownloaded(String status, List<Group> groups) {
          if(status.equals("SUCCESS")) {
            ModelingCommons.this.groups = groups;
            boolean enableAutoGeneratePreviewImage = workspace.getProcedures().get("SETUP") != null && workspace.getProcedures().get("GO") != null;
            JDialog uploadDialog = new UploadDialog(frame, ModelingCommons.this, error, enableAutoGeneratePreviewImage);
            uploadDialog.setVisible(true);
          } else if(status.equals("INVALID_RESPONSE_FROM_SERVER")) {
            promptForLogin("Invalid response from Modeling Commons");
          } else if(status.equals("CONNECTION_ERROR")) {
            promptForLogin("Could not connect to Modeling Commons");
          }
        }

      };
      request.execute();
    } else {
      boolean enableAutoGeneratePreviewImage = workspace.getProcedures().get("SETUP") != null && workspace.getProcedures().get("GO") != null;
      JDialog uploadDialog = new UploadDialog(frame, ModelingCommons.this, error, enableAutoGeneratePreviewImage);
      uploadDialog.setVisible(true);
    }
  }

  public void promptForSuccess(String error, String uploadedModelURL, String uploadedModelName) {
    JDialog successDialog = new UploadSuccessDialog(frame, this, error, uploadedModelURL, uploadedModelName);
    successDialog.setVisible(true);
  }

  public void promptForSuccess(String uploadedModelURL, String uploadedModelName) {
    promptForSuccess(" ", uploadedModelURL, uploadedModelName);
  }

  public void promptForCreateAccount(final String error) {
    Request request = new DownloadNewUserParametersRequest(getHttpClient(), frame) {
      @Override
      protected void onDownloaded(String status, String newUserAgreement, List<String> priorityCountries, List<String> unpriorityCountries) {
        if(status.equals("SUCCESS")) {
          ModelingCommons.this.newUserAgreement = newUserAgreement;
          ModelingCommons.this.priorityCountries = priorityCountries;
          ModelingCommons.this.unpriorityCountries = unpriorityCountries;
          JDialog createAccountDialog = new NewUserDialog(frame, ModelingCommons.this, error);
          createAccountDialog.setVisible(true);
        } else if(status.equals("INVALID_RESPONSE_FROM_SERVER")) {
          promptForLogin("Invalid response from Modeling Commons");
        } else if(status.equals("CONNECTION_ERROR")) {
          promptForLogin("Could not connect to Modeling Commons");
        }
      }
    };
    request.execute();
  }

  public void promptForCreateAccount() {
    promptForCreateAccount(" ");
  }

  public void saveToModelingCommons() {
    if(!isLoggedIn()) {
      promptForLogin();
    } else {
      promptForUpload();
    }
  }

  public Image getAutoGeneratedModelImage() {
    return new Image() {
      @Override
      public BufferedImage getImage() {
        return imageFunc.apply();
      }
    };
  }

  public Image getCurrentModelViewImage() {
    return new Image() {
      @Override
      public BufferedImage getImage() throws ImageException {
        return workspace.exportView();
      }
    };
  }

  //For use only in Modeling Commons requests
  HttpClient getHttpClient() {
    return http;
  }

}

