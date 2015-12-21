package com.okunev.lecturemanager;

/**
 * Created by 777 on 12/12/2015.
 */

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;

import uk.co.senab.photoview.PhotoViewAttacher;

public class FileBrowser extends AppCompatActivity implements AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener, AdapterView.OnItemSelectedListener,
        SimpleGestureFilter.SimpleGestureListener {
    private GridView mGrid;
    private File mCurrentDir;
    private ArrayList<File> mFiles;
    private String[] mAudioExt;
    private String[] mImageExt;
    private String[] mArchiveExt;
    private String[] mWebExt;
    private String[] mTextExt;
    private String[] mVideoExt;
    private String[] mGeoPosExt;
    private boolean mStandAlone;
    private IconView mLastSelected;
    private ArrayList<File> dirList = new ArrayList<>();
    private ArrayList<File> fileList = new ArrayList<>();
    private ImageView mImageView;
    private PhotoViewAttacher mAttacher;
    private int currentImage;
    private File wallpaperDirectory, mdir;
    private Display display;
    private SimpleGestureFilter detector;
    private String name;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_manager);
        mFiles = new ArrayList<>();

        mAudioExt = getResources().getStringArray(R.array.fileEndingAudio);
        mImageExt = getResources().getStringArray(R.array.fileEndingImage);
        mArchiveExt = getResources().getStringArray(R.array.fileEndingPackage);
        mWebExt = getResources().getStringArray(R.array.fileEndingWebText);
        mTextExt = getResources().getStringArray(R.array.fileEndingText);
        mVideoExt = getResources().getStringArray(R.array.fileEndingVideo);
        mGeoPosExt = getResources().getStringArray(R.array.fileEndingGeoPosition);

        Intent intent = getIntent();
        String action = intent.getAction();

        if (action == null || action.compareTo(Intent.ACTION_MAIN) == 0)
            mStandAlone = true;
        else
            mStandAlone = false;


        wallpaperDirectory = new File(Environment.getExternalStorageDirectory().getPath() + "/LectureManager/");
        wallpaperDirectory.mkdirs();
        File temp = new File(Environment.getExternalStorageDirectory().getPath() + "/LectureManager/.temp");
        temp.mkdirs();

        if (intent.getData() == null) browseTo(wallpaperDirectory);
        else  browseTo(new File(intent.getDataString()));


        display = getWindowManager().getDefaultDisplay();

        mGrid = (GridView) findViewById(R.id.gridView);
        mGrid.setNumColumns(display.getWidth() / 60);
        mGrid.setOnItemClickListener(this);
        mGrid.setOnItemLongClickListener(this);
        mGrid.setOnItemSelectedListener(this);
        IconAdapter iconAdapter = new IconAdapter();
        this.mGrid.setClickable(true);
        mGrid.setAdapter(iconAdapter);

        mImageView = (ImageView) findViewById(R.id.imageView);

        // Attach a PhotoViewAttacher, which takes care of all of the zooming functionality.
        mAttacher = new PhotoViewAttacher(mImageView);
        detector = new SimpleGestureFilter(this, this);
    }

    @Override
    public void onResume(){
        super.onResume();
        Bundle extras = getIntent().getExtras();

        try {
            browseTo(new File(extras.getString("DIR")));
        }
        catch (Exception l){
            browseTo(wallpaperDirectory);
        }
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent me) {
        // Call onTouchEvent of SimpleGestureFilter class
        this.detector.onTouchEvent(me);
        return super.dispatchTouchEvent(me);
    }

    @Override
    public void onSwipe(int direction) {
        switch (direction) {
            case SimpleGestureFilter.SWIPE_RIGHT:
                if (currentImage > 0) currentImage -= 1;
                else currentImage = fileList.size() - 1;
                mImageView.setImageURI(Uri.parse(fileList.get(currentImage).getPath()));
                mImageView.setBackgroundColor(0xffffffff);
                mAttacher.update();
                break;
            case SimpleGestureFilter.SWIPE_LEFT:
                if (currentImage < fileList.size() - 1) currentImage++;
                else currentImage = 0;
                mImageView.setImageURI(Uri.parse(fileList.get(currentImage).getPath()));
                mImageView.setBackgroundColor(0xffffffff);
                mAttacher.update();
                break;
            case SimpleGestureFilter.SWIPE_DOWN:
                Animation anim = AnimationUtils.loadAnimation(this, R.anim.img_anim);
//Animation animation = new ScaleAnimation(1.0f,0.0f,1.0f,1.0f,Animation.RELATIVE_TO_PARENT,0.5f,Animation.RELATIVE_TO_PARENT,0.5f);
                //animation.setDuration(300);
                mImageView.setAnimation(anim);
                mImageView.startAnimation(anim);
                mImageView.setVisibility(View.INVISIBLE);
                mAttacher.update();
                browseTo(wallpaperDirectory);
                break;
            case SimpleGestureFilter.SWIPE_UP:
                browseTo(mdir);
                break;
        }
    }

    @Override
    public void onDoubleTap() {
    }

    private synchronized void browseTo(final File location) {
        mCurrentDir = location;

        mFiles.clear();
        dirList.clear();
        fileList.clear();

        this.setTitle(mCurrentDir.getName().compareTo("") == 0 ? mCurrentDir.getPath() : mCurrentDir.getName());
        mdir = mCurrentDir;
        if (!location.getParentFile().getPath().equals(Environment.getExternalStorageDirectory().getPath())) {
            mFiles.add(mCurrentDir.getParentFile());
            mdir = mCurrentDir;
        }
        for (File file : mCurrentDir.listFiles()) {
            if (file.isDirectory() & !file.getName().startsWith(".")) {
                dirList.add(file);
            } else if (file.getName().contains(".lecture")) {
                fileList.add(file);
            }
        }
        Collections.sort(dirList);
        Collections.sort(fileList);

        for (File fik : dirList) {
            mFiles.add(fik);
        }
        for (File fik : fileList) {
            mFiles.add(fik);
        }

        if (mGrid != null) mGrid.setAdapter(new IconAdapter());
    }


    public class IconAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mFiles.size();
        }

        @Override
        public Object getItem(int index) {
            return mFiles.get(index);
        }

        @Override
        public long getItemId(int index) {
            return index;
        }

        @Override
        public View getView(int index, View convertView, ViewGroup parent) {
            IconView icon;
            File currentFile = mFiles.get(index);

            int iconId;
            String filename;

            if (index == 0 && (!mCurrentDir.getAbsolutePath().equals(Environment.getExternalStorageDirectory().getPath() + "/LectureManager"))) {
                iconId = R.drawable.updirectory;
                filename = new String("..");
            } else {
                filename = currentFile.getName();
                iconId = getIconId(index);
            }
            if (currentFile.getName().endsWith(".jpg") || currentFile.getName().endsWith(".lecture"))
                try {
                    final int THUMBSIZE = 64;
                    Bitmap allah = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(currentFile.getPath()),
                            THUMBSIZE, THUMBSIZE);
                    icon = new IconView(FileBrowser.this, allah, filename);
                    return icon;
                } catch (Exception e) {
                    e.printStackTrace();
                }

            if (convertView == null) {
                icon = new IconView(FileBrowser.this, iconId, filename);
            } else {
                icon = (IconView) convertView;
                icon.setIconResId(iconId);
                icon.setFileName(filename);
            }

            return icon;
        }

        private int getIconId(int index) {
            File file = mFiles.get(index);

            if (file.isDirectory()) return R.drawable.directory;

            return R.drawable.unknown;
        }

    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long id) {
        File file = mFiles.get((int) id);

        if (file.isDirectory()) {
            browseTo(file);
        } else {
            if (!mStandAlone) {
                // Send back the file that was selected
                Uri path = Uri.fromFile(file);
                Intent intent = new Intent(Intent.ACTION_PICK, path);
                setResult(RESULT_OK, intent);
                finish();
            } else {
                // Try to open it
               /* Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setDataAndType(Uri.fromFile(file), getMimeType(file));
                startActivity(Intent.createChooser(intent, null));*/
                currentImage = (int) id - dirList.size();
                mImageView.setImageURI(Uri.parse(fileList.get(currentImage).getPath()));
                mImageView.setVisibility(View.VISIBLE);
                mImageView.setBackgroundColor(0xffffffff);
                mAttacher.update();

            }
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parentView, View view, int arg2, final long id) {
        final File file = mFiles.get((int) id);
        final File parent = file.getParentFile();

        new AlertDialog.Builder(FileBrowser.this)
                .setIcon(android.R.drawable.ic_menu_agenda)
                .setItems(R.array.file_options, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        switch (whichButton) {
                            case 0: // Rename
                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(FileBrowser.this);
                                alertDialog.setTitle("Rename file");
                                alertDialog.setMessage("Enter name");

                                final EditText input = new EditText(FileBrowser.this);
                                input.setText(file.getName().replace(".lecture",""));
                                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.MATCH_PARENT);
                                input.setLayoutParams(lp);
                                alertDialog.setView(input);
                                alertDialog.setIcon(R.drawable.rename);

                                alertDialog.setPositiveButton("Rename",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (!file.isDirectory())
                                                    file.renameTo(new File(parent, input.getText().toString() + ".lecture"));
                                                else
                                                    file.renameTo(new File(parent, input.getText().toString()));
                                                browseTo(parent);
                                            }
                                        });

                                alertDialog.setNegativeButton("Cancel",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        });
                                alertDialog.show();
                                break;
                            case 1: // Delete

                                AlertDialog.Builder alertDialog1 = new AlertDialog.Builder(FileBrowser.this);
                                alertDialog1.setTitle("Delete file?");
                                alertDialog1.setIcon(R.drawable.delete);

                                alertDialog1.setPositiveButton("Delete",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                file.delete();
                                                browseTo(parent);
                                            }
                                        });

                                alertDialog1.setNegativeButton("Cancel",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        });
                                alertDialog1.show();
                                break;
                            case 2: // Cut
                                // Environment.getExternalStorageDirectory().getPath() + "/LectureManager/.temp"
                                name = file.getName();
                                File to = new File(Environment.getExternalStorageDirectory().getPath() + "/LectureManager/.temp/temp.lecture");
                                file.renameTo(to);
                                browseTo(parent);
                                // mGrid.removeViewAt((int) id);
                                break;
                            case 3: // Copy
                                name = file.getName();
                                File copied = file;
                                File temped = new File(Environment.getExternalStorageDirectory().getPath() + "/LectureManager/.temp/temp.lecture");
                                try {
                                    copyDirectory(copied, temped);
                                    Toast.makeText(FileBrowser.this, "Copied!", Toast.LENGTH_LONG).show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                browseTo(parent);
                                break;
                            case 4: // Send To...
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.setType("image/jpeg");
                                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                                startActivity(Intent.createChooser(intent, null));
                                break;
                        }
                        dialog.dismiss();
                    }
                })
                .create().show();
        return true;
    }

    public void copyDirectory(File sourceLocation, File targetLocation)
            throws IOException {

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists() && !targetLocation.mkdirs()) {
                throw new IOException("Cannot create dir " + targetLocation.getAbsolutePath());
            }

            String[] children = sourceLocation.list();
            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]),
                        new File(targetLocation, children[i]));
            }
        } else {

            // make sure the directory we plan to store the recording in exists
            File directory = targetLocation.getParentFile();
            if (directory != null && !directory.exists() && !directory.mkdirs()) {
                throw new IOException("Cannot create dir " + directory.getAbsolutePath());
            }

            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> grid, View icon, int arg2, long index) {
        if (mLastSelected != null) {
            mLastSelected.deselect();
        }

        if (icon != null) {
            mLastSelected = (IconView) icon;
            mLastSelected.select();
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> grid) {
        if (mLastSelected != null) {
            mLastSelected.deselect();
            mLastSelected = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_file, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        mImageView.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.about) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("DIR", mdir.getPath());
            startActivity(intent);
        }
        if (id == R.id.paste) {
            File from = new File(Environment.getExternalStorageDirectory().getPath() + "/LectureManager/.temp/temp.lecture");
            File to = new File(mdir.getPath() + "/" + name);
            from.renameTo(to);
            browseTo(mdir);
        }
        if (id == R.id.newf) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(FileBrowser.this);
            alertDialog.setTitle("CREATE FOLDER");
            alertDialog.setMessage("Enter Name");

            final EditText input = new EditText(FileBrowser.this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            input.setLayoutParams(lp);
            alertDialog.setView(input);
            alertDialog.setIcon(R.drawable.about);

            alertDialog.setPositiveButton("YES",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            File old = new File(Environment.getExternalStorageDirectory().getPath() + "/LectureManager/");
                            File wallpaperDirectory = new File(Environment.getExternalStorageDirectory().getPath() + "/LectureManager/" + input.getText().toString() + "/");
                            wallpaperDirectory.mkdirs();
                            browseTo(old);
                        }
                    });

            alertDialog.setNegativeButton("NO",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

            alertDialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        name = "";
        File from = new File(Environment.getExternalStorageDirectory().getPath() + "/LectureManager/.temp/temp.lecture");
        if (from.exists()) from.delete();
    }
}
