package com.mdstudios.diabeticons.Core;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mdstudios.diabeticons.R;
import com.mdstudios.diabeticons.Utils.Util;

import java.io.IOException;
import java.util.LinkedList;


public class MainActivity extends ActionBarActivity {
  private static final String LOGTAG = "MD/MainActivity";

  // The Actionbar-replacement Toolbar that runs along the top of the screen
  Toolbar mToolbar;

  ListView mListView;
  IconListAdapter mAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Add the toolbar
    mToolbar = (Toolbar) findViewById(R.id.toolbar);
    if (mToolbar != null) {
      setSupportActionBar(mToolbar);
    }

    // TODO: Check if there is saved data- if so, then don't redo everything unnecessarily
    // Set up the basic content
    initContent();
  }

  private void initContent() {
    // Grab the ListView, so can set it up
    mListView = (ListView) findViewById(R.id.listview);

    // Give it the appropriate adapter
    mAdapter = new IconListAdapter(this);
    mListView.setAdapter(mAdapter);

    // Set the click listener for the ListView
    mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Get the title and path to the image from the adapter
        String path = mAdapter.getPath(position);
        String title = mAdapter.getTitle(position);

        Intent intent = new Intent(MainActivity.this, SendActivity.class);
        intent.putExtra(Util.KEY_PATH, path);
        intent.putExtra(Util.KEY_TITLE, title);
        startActivity(intent);
      }
    });

    //testAllImages();
  }

  // Prints out all the target images from assets to the log
  private void testAllImages() {
    AssetManager assetManager = getAssets(); // Necessary to access assets
    try {
      // Get all the file names
      String[] files = assetManager.list("diabeticons"); // "diabeticons" = path

      // For now, print out all the names
      int i = 0;
      for(String name : files) {
        // Print out the name + the index
        Log.d(LOGTAG, "Index " + i + ": " + name);

        // Testing out chopping off the last bunch of the name
        String simplerName = name.substring(0, name.indexOf('.'));
        // Looks for a lower case followed by an upper case, adds space between
        // Source: http://stackoverflow.com/questions/4886091/insert-space-after-capital-letter
        String simplerOutput = simplerName.replaceAll("(\\p{Ll})(\\p{Lu})","$1 $2");
        Log.d(LOGTAG, "Simplified: \"" + simplerOutput + "\"");

        // Increment the index
        ++i;
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_activity_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  private class IconListAdapter extends BaseAdapter {
    Context mContext;

    LinkedList<String> mPaths;      // The assets path for each item
    LinkedList<String> mTitles;     // The display-ready title for each item
    LinkedList<Drawable> mImages;   // All the images for each item

    public IconListAdapter(Context context) {
      this.mContext = context;

      // Get all the necessary images and titles
      AssetManager assetManager = context.getAssets(); // Necessary to access assets
      try {
        // Get all the file names
        String[] files = assetManager.list("diabeticons"); // "diabeticons" = path

        // Save all the names and images
        mPaths = new LinkedList<>();
        mTitles = new LinkedList<>();
        mImages = new LinkedList<>();
        for(int i = 0; i < files.length; ++i) {
          // Cache the image for this item from its file path
          String path = "diabeticons/" + files[i];
          Drawable d = Drawable.createFromStream(getAssets().open(path), null);
          mImages.add(d);

          // Cache the path, for later use
          mPaths.add(path);

          // Get the simplified, displayable name (no .filetype, no nyNameIsFrankenstein)
          String simplerName = files[i].substring(0, files[i].indexOf('.'));
            // Looks for a lower case followed by an upper case, adds space between
            // Source: http://stackoverflow.com/questions/4886091/insert-space-after-capital-letter
          String simplerNameFinal = simplerName.replaceAll("(\\p{Ll})(\\p{Lu})","$1 $2");

          // Save the easier-to-read title
          mTitles.add(simplerNameFinal);
        }

      } catch (IOException e) {
        Log.e(LOGTAG, "There was an error! Error: " + e.toString());
        e.printStackTrace();
      }
    }

    public String getPath(int position) {
      return mPaths.get(position);
    }

    public Drawable getImage(int position) {
      return mImages.get(position);
    }

    public String getTitle(int position) {
      return mTitles.get(position);
    }

    @Override
    public int getCount() {
      return mTitles.size();
    }

    @Override
    public Object getItem(int position) {
      return null;
    }

    @Override
    public long getItemId(int position) {
      return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      View row = null;
      ViewHolder holder;

      if(convertView == null) {
        // Then gotta set up this row for the first time
        LayoutInflater inflater =
            (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        row = inflater.inflate(R.layout.list_itemselect, parent, false);

        // Create a ViewHolder to save all the different parts of the row
        holder = new ViewHolder();
        holder.icon = (ImageView) row.findViewById(R.id.icon);
        holder.title = (TextView) row.findViewById(R.id.title);

        // Make the row reuse the ViewHolder
        row.setTag(holder);
      }
      else { // Otherwise, use the recycled view
        row = convertView;
        holder = (ViewHolder) row.getTag();
      }

      // Set the title and icon of this item according to the position
      holder.title.setText(getTitle(position));
      holder.icon.setImageDrawable(getImage(position));

      return row;
    }

    class ViewHolder{
      public ImageView icon;
      public TextView title;
    }
  }
}
