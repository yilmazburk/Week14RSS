package com.istinye.week14rss;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.istinye.week14rss.models.News;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private ListView newsListView;
    private BaseAdapter listAdapter;
    private List<News> newsList;

    private Button detailButton;
    private Button permissionButton;

    public static final int WRITE_EXTERNAL_REQUEST_CODE = 2323;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Anadolu Ajans News");

        newsList = new ArrayList<>();

        initViews();

        new RSSFetchAsyncTask().execute("https://www.aa.com.tr/tr/rss/default?cat=guncel");

    }

    private void initViews() {
        newsListView = findViewById(R.id.newsListView);
        listAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return newsList.size();
            }

            @Override
            public Object getItem(int position) {
                return newsList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                ViewHolder viewHolder;

                if (convertView == null) {
                    convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_listview_item, parent, false);
                    viewHolder = new ViewHolder(convertView);
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolder) convertView.getTag();
                }

                News news = newsList.get(position);

                viewHolder.titleTextView.setText(news.getTitle());
                viewHolder.descriptionTextView.setText(news.getDescription());

                return convertView;
            }
        };
        newsListView.setAdapter(listAdapter);

        newsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String url = newsList.get(position).getUrl();

                Intent detailIntent = new Intent(MainActivity.this, DetailActivity.class);
                detailIntent.putExtra("URL", url);
                startActivity(detailIntent);
            }
        });

        initButtons();
    }



    private class RSSFetchAsyncTask extends AsyncTask<String, Void, Exception> {

        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        Exception exception = null;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("News fetching...");
            progressDialog.show();
        }

        @Override
        protected Exception doInBackground(String... strings) {

            try {
                URL newsUrl = new URL(strings[0]);

                XmlPullParserFactory xmlPullParserFactory = XmlPullParserFactory.newInstance();
                xmlPullParserFactory.setNamespaceAware(true);

                XmlPullParser parser = xmlPullParserFactory.newPullParser();
                parser.setInput(newsUrl.openConnection().getInputStream(), "UTF_8");

                boolean inItemTag = false;
                News newsObject = null;
                int eventType = parser.getEventType();

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        if (parser.getName().equalsIgnoreCase("item")) {
                            inItemTag = true;
                            newsObject = new News();
                        } else if (parser.getName().equalsIgnoreCase("link")) {
                            if (inItemTag) {
                                newsObject.setUrl(parser.nextText());
                            }
                        } else if (parser.getName().equalsIgnoreCase("title")) {
                            if (inItemTag) {
                                newsObject.setTitle(parser.nextText());
                            }
                        } else if (parser.getName().equalsIgnoreCase("description")) {
                            if (inItemTag) {
                                newsObject.setDescription(parser.nextText());
                            }
                        }
                    } else if (eventType == XmlPullParser.END_TAG && parser.getName().equalsIgnoreCase("item")) {
                        inItemTag = false;
                        newsList.add(newsObject);
                    }
                    eventType = parser.next();
                }
            } catch (MalformedURLException e) {
                exception = e;
            } catch (XmlPullParserException e) {
                exception = e;
            } catch (IOException e) {
                exception = e;
            }


            return exception;
        }

        @Override
        protected void onPostExecute(Exception e) {
            super.onPostExecute(e);

            if (e != null) {
                Toast.makeText(MainActivity.this, "Fetching operation failed cause of " + e.getMessage(), Toast.LENGTH_SHORT).show();
            } else {
                listAdapter.notifyDataSetChanged();
            }
            progressDialog.dismiss();
        }
    }


    private class ViewHolder {
        private TextView titleTextView;
        private TextView descriptionTextView;

        public ViewHolder (View convertView) {
            titleTextView = convertView.findViewById(R.id.newsTitle);
            descriptionTextView = convertView.findViewById(R.id.newsDescription);
        }
    }


    private void initButtons() {

        detailButton = findViewById(R.id.detailButton);
        detailButton.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent detailIntent = new Intent(MainActivity.this, DetailActivity.class);
                detailIntent.putExtra("URL", "https://www.aa.com.tr/tr/guncel/ayvalikta-dun-etkili-olan-firtina-nedeniyle-80-teknenin-battigi-belirlendi/2250946");
                startActivity(detailIntent);
            }
        });


        permissionButton = findViewById(R.id.permissionButton);
        permissionButton.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, WRITE_EXTERNAL_REQUEST_CODE);
                } else {
                    Toast.makeText(MainActivity.this, "You have already permission", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == WRITE_EXTERNAL_REQUEST_CODE && grantResults.length != 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "Thank you for permission", Toast.LENGTH_SHORT).show();
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                    AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
                    myDialog.setMessage("Permission is important to save data on your phone. Please give us permission.");
                    myDialog.setTitle("Important Permission Warning");

                    myDialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, WRITE_EXTERNAL_REQUEST_CODE);
                        }
                    });
                    myDialog.setNegativeButton("No, I'm not sure", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(MainActivity.this, "You can not use application effectively.", Toast.LENGTH_SHORT).show();
                        }
                    });

                    myDialog.show();

                } else {
                    Toast.makeText(MainActivity.this, "You should give us this permission to use application.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}