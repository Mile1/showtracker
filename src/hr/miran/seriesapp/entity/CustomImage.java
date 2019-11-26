package hr.miran.seriesapp.entity;

import javafx.scene.image.ImageView;

public class CustomImage 
{
     private ImageView image;
     private String string;

     public CustomImage(ImageView img, String string) 
     {
         this.image = img;
         this.string = string;
     }

     public void setImage(ImageView value) 
     {
         image = value;
     }

     public ImageView getImage() 
     {
         return image;
     }

     public void setSring(String string) 
     {
         this.string = string;
     }

     public String getString() 
     {
         return this.string;
     }
 }