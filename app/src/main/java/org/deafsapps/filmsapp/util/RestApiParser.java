package org.deafsapps.filmsapp.util;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;

import org.deafsapps.filmsapp.R;

import org.deafsapps.filmsapp.activities.MainActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// AsyncTask<TypeOfVarArgParams, ProgressValue, ResultValue>
public class RestApiParser extends AsyncTask<Object, Void, List<FilmItem>> implements TraktJsonHeaders
{
    private static final String TAG_REST_API_PARSER = "In-RestApiParser";

    private Context appContext;
    private Snackbar mLoadingSnackbar;

    // This interface will allow to return a 'Film' List to 'MainActivity'
    public interface OnParserAsyncResponse
    {
        void onAsyncResponse(List<FilmItem> dataItemList, String date);
    }
    // The interface is implemented by the entity which is receiving the response, and a field is created in the "sender"
    private OnParserAsyncResponse mParserResponse;

    public RestApiParser(Context mContext)
    {
        this.appContext = mContext;
        this.mParserResponse = (OnParserAsyncResponse) mContext;
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();

        this.mLoadingSnackbar = Snackbar.make(((MainActivity) this.appContext).findViewById(R.id.appCoordLayout), "Loading...", Snackbar.LENGTH_INDEFINITE);
        this.mLoadingSnackbar.show();
    }

    @Override
    protected List<FilmItem> doInBackground(Object[] params)
    {
        while (!this.isCancelled())
        {
            try
            {
                // The input arguments are fetched in order
                Log.i(RestApiParser.TAG_REST_API_PARSER, "URL to be queried: " + params[0]);
                //String urlString = URLEncoder.encode((String) params[0], "UTF-8");
                URL myUrl = new URL((String) params[0]);   // Throws 'MalformedURLException'
                boolean isSearchQuery = (boolean) params[1];

                if (this.isCancelled()) break;   // In case the user is still typing and the 'AsyncTask' has to be cancelled

                HttpURLConnection myConnection = (HttpURLConnection) myUrl.openConnection();   // Throws 'IOException'
                    myConnection.setRequestMethod("GET");
                    myConnection.setConnectTimeout(3000);
                    myConnection.addRequestProperty("Content-type", "application/json");
                    myConnection.addRequestProperty("trakt-api-version", "2");
                    myConnection.addRequestProperty("trakt-api-key", this.appContext.getString(R.string.trakt_api_key));

                if (this.isCancelled()) break;

                int respCode = myConnection.getResponseCode();   // Throws 'IOException'
                Log.e(RestApiParser.TAG_REST_API_PARSER, "The response is: " + respCode);
                //Log.i(RestApiParser.TAG_REST_API_PARSER, "The headers are: " + myConnection.getHeaderFields());

                if (respCode == HttpURLConnection.HTTP_OK)
                {
                    StringBuilder resultJsonString = new StringBuilder();

                    InputStream myInStream = myConnection.getInputStream();   // Throws 'IOException'
                    BufferedReader myBufferedReader = new BufferedReader(new InputStreamReader(myInStream));

                    if (this.isCancelled()) break;

                    String line;
                    while ((line = myBufferedReader.readLine()) != null) { resultJsonString.append(line).append("\n"); }
                    myInStream.close();   // Always close the 'InputStream'

                    if (this.isCancelled()) break;

                    myConnection.disconnect();

                    if (resultJsonString.toString() != null)
                        return parseJsonString(resultJsonString.toString(), isSearchQuery);

                    break;
                }
                else
                {
                    myConnection.disconnect();
                    break;    // In case the query has had no success at all
                }
            }
            catch (java.net.SocketTimeoutException e) { return null; }
            catch (java.io.IOException e) { return null; }
        }

        return null;
    }

    @Override
    protected void onPostExecute(@Nullable List<FilmItem> mList)
    {
        super.onPostExecute(mList);

        if (mList != null)
        {
            this.mParserResponse.onAsyncResponse(mList, "Last update " + new SimpleDateFormat("d MMM yyyy HH:mm", Locale.getDefault()).format(new Date()));
            this.mLoadingSnackbar.dismiss();
        }
        else
        {
            Log.w(RestApiParser.TAG_REST_API_PARSER, "Error loading feed");

            // This 'Snackbar' will dismiss the one currently on display (saying "Loading...")
            Snackbar.make(((MainActivity) this.appContext).findViewById(R.id.appCoordLayout), "Error loading feed", Snackbar.LENGTH_SHORT).show();
        }
    }

    @Nullable
    private List<FilmItem> parseJsonString(String jsonString, boolean isSearchQuery)
    {
        List<FilmItem> mFilmList = new ArrayList<>();

        try
        {
            JSONArray mFilms = new JSONArray(jsonString);
            Log.i(RestApiParser.TAG_REST_API_PARSER, "JSON array size: " + String.valueOf(mFilms.length()));

            // looping through all Movies
            for (int idx = 0; idx < mFilms.length(); idx++)
            {
                JSONObject mRootObject = mFilms.getJSONObject(idx);
                // In case it is a "search" query, the JSON string format is slightly different
                if (isSearchQuery) { mRootObject = mRootObject.getJSONObject(TraktJsonHeaders.MOVIE); }
                    String mTitle = mRootObject.getString(TraktJsonHeaders.TITLE);
                    String mYear = mRootObject.getString(TraktJsonHeaders.YEAR);
                    String mOverview = mRootObject.getString(TraktJsonHeaders.OVERVIEW);
                    String mTrailer = mRootObject.getString(TraktJsonHeaders.TRAILER);
                    float mRating = (float) mRootObject.getDouble(TraktJsonHeaders.RATING);
                    long mVotes = mRootObject.getLong(TraktJsonHeaders.VOTES);

                // "ids" node is a JSON Object
                JSONObject mIdsObject = mRootObject.getJSONObject(TraktJsonHeaders.IDS);
                    int mTrakt = mIdsObject.getInt(TraktJsonHeaders.TRAKT);
                    String mSlug = mIdsObject.getString(TraktJsonHeaders.SLUG);
                    String mImdb = mIdsObject.getString(TraktJsonHeaders.IMDB);
                    int mTmdb = mIdsObject.getInt(TraktJsonHeaders.TMDB);

                // "images" node is a JSON Object
                JSONObject mImgObject = mRootObject.getJSONObject(TraktJsonHeaders.IMAGES);
                JSONObject mPosterObject = mImgObject.getJSONObject(TraktJsonHeaders.POSTER);
                    String mPoster = mPosterObject.getString(TraktJsonHeaders.THUMB);
                JSONObject mLogoObject = mImgObject.getJSONObject(TraktJsonHeaders.LOGO);
                    String mLogo = mLogoObject.getString(TraktJsonHeaders.FULL);

                // Creating 'FilmItem' objects and populating the List
                mFilmList.add(new FilmItem(mTitle, mYear, mTrakt, mSlug, mImdb, mTmdb, mOverview, mTrailer, mRating, mVotes, mPoster, mLogo));
            }

            return mFilmList;

        } catch (JSONException e) { e.printStackTrace(); }

        return null;
    }

    @Override
    protected void onCancelled()
    {
        super.onCancelled();

        Log.i(RestApiParser.TAG_REST_API_PARSER, "Query cancelled");
    }
}
