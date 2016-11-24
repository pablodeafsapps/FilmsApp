package org.deafsapps.filmsapp.activities;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.deafsapps.filmsapp.R;
import org.deafsapps.filmsapp.util.FilmItem;
import org.deafsapps.filmsapp.util.RestApiParser;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, RestApiParser.OnParserAsyncResponse, SwipeRefreshLayout.OnRefreshListener, TextWatcher
{
    private static final String TAG_MAIN_ACTIVITY = "In-MainActivity";
    private static final String REST_API_POPULAR_URL = "https://api.trakt.tv/movies/popular?extended=full,images";
    private static final String REST_API_SEARCH_URL = "https://api.trakt.tv/search/movie?extended=full,images";

    private AsyncTask<Object, Void, List<FilmItem>> mTask;
    private EditText mSearchEditText;
    private ImageView mClearImageView;
    private SwipeRefreshLayout mSwipeLayout;
    private RecyclerView mRecyclerView;
    private boolean isSearchQuery = false;   // Initially the search box is empty!
    private static int listPage = 1;
    private String searchKeyword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.mSearchEditText = (EditText) this.findViewById(R.id.searchEditText);
            this.mSearchEditText.addTextChangedListener(this);
        this.mClearImageView = (ImageView) this.findViewById(R.id.editTextImageView);
            this.mClearImageView.setOnClickListener(this);
        this.mSwipeLayout = (SwipeRefreshLayout) this.findViewById(R.id.swipeContainer);
            this.mSwipeLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorAccent));
            this.mSwipeLayout.setOnRefreshListener(this);
        this.mRecyclerView = (RecyclerView) this.findViewById(R.id.recyclerViewMain);

        //----- TOOLBAR -----
        // Getting a reference to the 'Toolbar' and adding it as ActionBar for the 'Activity'
        final Toolbar mToolbar = (Toolbar) this.findViewById(R.id.appToolbar);
        // This coming line makes the magic, replacing the 'ActionBar' with the 'Toolbar'
        this.setSupportActionBar(mToolbar);
        //--------------------------------------

        //----- RECYCLER_VIEW -----
        // Define now how children are organised (in 'LinearLayout', in a 'GridLayout', or in a 'StaggeredLinearLayout'
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        this.mRecyclerView.setLayoutManager(mLayoutManager);
        this.mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                super.onScrolled(recyclerView, dx, dy);

                // Configuring scroll events in case there are items in the list
                if (recyclerView.getAdapter().getItemCount() != 0)
                {
                    if ((mLayoutManager.findLastCompletelyVisibleItemPosition() == recyclerView.getAdapter().getItemCount() - 1) && (dy > 0))
                    {
                        listPage++;
                        MainActivity.this.queryRestApiBuilder(MainActivity.this, listPage, isSearchQuery, searchKeyword);
                    }
                    else if ((listPage != 1 && mLayoutManager.findFirstCompletelyVisibleItemPosition() == 0) && (dy < 0))
                    {
                        listPage--;
                        MainActivity.this.queryRestApiBuilder(MainActivity.this, listPage, isSearchQuery, searchKeyword);
                    }
                }
            }
        });
        this.mRecyclerView.setAdapter(new MyListAdapterRecycler(this, new ArrayList<FilmItem>(), ""));
        //--------------------------------------

        // Loading the 10 most popular movies by default
        this.queryRestApiBuilder(this, this.listPage, this.isSearchQuery, this.searchKeyword);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater mInflater = this.getMenuInflater();
            mInflater.inflate(R.menu.menu_options, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem whichItem)
    {
        if (whichItem.getItemId() == R.id.menu_option_refresh)
        {
            Log.i(MainActivity.TAG_MAIN_ACTIVITY, "REFRESH clicked");

            this.queryRestApiBuilder(this, this.listPage, this.isSearchQuery, this.searchKeyword);
        }
        else
            return false;

        return true;
    }

    @Override
    public void onClick(View whichView)
    {
        if (whichView.getId() == R.id.editTextImageView)
        {
            if (this.mSearchEditText.getText().toString().trim().length() > 0)
                this.mSearchEditText.setText("");
        }
    }

    private void queryRestApiBuilder(Context mContext, int whichPage, boolean searchFlag, String whichKeyword)
    {
        // Getting the 'AsyncTask' reference so that it can be cancellable
        this.mTask = new RestApiParser(mContext);

        if (searchFlag)
            this.mTask.execute(MainActivity.REST_API_SEARCH_URL + "&query=" + whichKeyword + "&page=" + String.valueOf(whichPage) + "&limit=10", searchFlag);
        else
            this.mTask.execute(MainActivity.REST_API_POPULAR_URL + "&page=" + String.valueOf(whichPage) + "&limit=10", searchFlag);
    }

    @Override
    public void onAsyncResponse(List<FilmItem> loadedFilmList, String date)
    {
        Log.i(MainActivity.TAG_MAIN_ACTIVITY, "onAsyncResponse");

        // Once loaded, the 'RecyclerView' object is updated through its adapter
        ((MyListAdapterRecycler) this.mRecyclerView.getAdapter()).setmDate(date);
        ((MyListAdapterRecycler) this.mRecyclerView.getAdapter()).setItemList(loadedFilmList);
        this.mRecyclerView.getAdapter().notifyDataSetChanged();

        // Moving back the scroll to the top of the list
        this.mRecyclerView.scrollToPosition(0);

        // Dismiss 'SwipeRefreshLayout' spinner if it is ON
        if (this.mSwipeLayout.isShown())
            this.mSwipeLayout.setRefreshing(false);

        // Once loaded, the 'AppBarLayout' is shown completely again
        ((AppBarLayout) this.findViewById(R.id.appBarLayout)).setExpanded(true, true);

        Toast.makeText(this, "Page " + this.listPage, Toast.LENGTH_SHORT).show();
    }

    // This method corresponds to the 'SwipeRefreshLayout.OnRefreshListener' interface
    @Override
    public void onRefresh()
    {
        Log.i(MainActivity.TAG_MAIN_ACTIVITY, "Swipe refresh request");

        this.queryRestApiBuilder(this, this.listPage, this.isSearchQuery, this.searchKeyword);
    }

    // These three methods correspond to the 'TextWatcher' interface
    @Override
    public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {  }

    @Override
    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {  }

    @Override
    public void afterTextChanged(Editable editable)
    {
        Log.i(MainActivity.TAG_MAIN_ACTIVITY, "afterTextChanged");

        // If there is any 'AsyncTask' running, it has to be stopped/cancelled
        this.mTask.cancel(true);

        this.listPage = 1;

        // If the search 'EditText' is not empty, a "search query" will be performed and the clear 'ImageView' is shown
        if (editable.toString().trim().length() != 0)
        {
            this.isSearchQuery = true;
            if (!this.mClearImageView.isShown()) { this.mClearImageView.setVisibility(View.VISIBLE); }
        }
        else
        {
            this.isSearchQuery = false;
            this.mClearImageView.setVisibility(View.INVISIBLE);
        }

        this.searchKeyword = editable.toString();

        queryRestApiBuilder(this, this.listPage, this.isSearchQuery, this.searchKeyword);
    }

    // This adapter uses the 'RecyclerView.Adapter' for the 'RecyclerView' (which is an optimized 'ListView')
    private class MyListAdapterRecycler extends RecyclerView.Adapter<RecyclerView.ViewHolder>
    {
        private static final int TYPE_HEADER = 0;
        private static final int TYPE_CARD = 1;

        private class MyCardTopMessageViewHolder extends RecyclerView.ViewHolder
        {
            private TextView updateMessage_TxtView;

            public MyCardTopMessageViewHolder(View itemView)
            {
                super(itemView);

                this.updateMessage_TxtView = (TextView) itemView.findViewById(R.id.headerRecyclerViewTextView);
            }
        }

        // Creating a 'ViewHolder' to speed up the performance
        public class MyCardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
        {
            private View cardView;
            private ImageView icon_ImgView;
            private TextView title_TxtView;
            private TextView body_TxtView;
            private TextView release_TxtView;

            public MyCardViewHolder(View itemView)
            {
                super(itemView);

                this.cardView = itemView;
                    this.cardView.setOnClickListener(this);
                this.icon_ImgView = (ImageView) itemView.findViewById(R.id.card_image);
                this.title_TxtView = (TextView) itemView.findViewById(R.id.card_title);
                this.body_TxtView = (TextView) itemView.findViewById(R.id.card_body);
                this.release_TxtView = (TextView) itemView.findViewById(R.id.card_rel_date);
            }

            @Override
            public void onClick(View whichView)
            {
                if(whichView.equals(this.cardView))
                {
                    Intent detailIntent = new Intent(mContext, DetailActivity.class);
                        detailIntent.putExtra("title", itemList.get(this.getAdapterPosition() - 1).getFilmTitle());
                        detailIntent.putExtra("overview", itemList.get(this.getAdapterPosition() - 1).getFilmOverview());
                        detailIntent.putExtra("year", itemList.get(this.getAdapterPosition() - 1).getFilmYear());
                        detailIntent.putExtra("rating", itemList.get(this.getAdapterPosition() - 1).getFilmRating());
                        detailIntent.putExtra("imdb", itemList.get(this.getAdapterPosition() - 1).getFilmIds().getFilmIdImdb());
                        detailIntent.putExtra("poster", itemList.get(this.getAdapterPosition() - 1).getFilmImages().getFilmPoster());
                        detailIntent.putExtra("logo", itemList.get(this.getAdapterPosition() - 1).getFilmImages().getFilmLogo());
                    startActivity(detailIntent);
                }
            }
        }

        private Context mContext;
        private List<FilmItem> itemList;
        private String mDate;

        // Provide a suitable constructor (depends on the kind of dataset)
        public MyListAdapterRecycler(Context context, List<FilmItem> objects, String objectDate) {
            this.mContext = context;
            this.itemList = objects;
            this.mDate = objectDate;
        }

        public Context getmContext() { return mContext; }

        public List<FilmItem> getItemList() { return itemList; }
        public void setItemList(List<FilmItem> itemList) { this.itemList = itemList; }

        public String getmDate() { return this.mDate; }
        public void setmDate(String mDate) { this.mDate = mDate; }

        // Create new views (invoked by the layout manager)
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            if (viewType == MyListAdapterRecycler.TYPE_HEADER)
            {
                // create a new header
                View viewRow = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_top_message, parent, false);
                return new MyCardTopMessageViewHolder(viewRow);
            }
            else if (viewType == MyListAdapterRecycler.TYPE_CARD)
            {
                // create a new view
                final View viewRow = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_custom_layout, parent, false);
                // set the view's size, margins, padding and layout parameters
                return new MyCardViewHolder(viewRow);
            }

            return null;
        }


        @Override
        public int getItemViewType(int position)
        {
            if (position == 0)
                return MyListAdapterRecycler.TYPE_HEADER;
            else
                return MyListAdapterRecycler.TYPE_CARD;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
        {
            if (position == MyListAdapterRecycler.TYPE_HEADER)
            {
                ((MyCardTopMessageViewHolder) holder).updateMessage_TxtView.setText(this.getmDate());
            }
            else
            {
                // get element from the dataset at this position and replace the contents of the view with that element
                // 'position - 1' is employed so that the "date TextView" on top of the list is taken into account
                // Using 'Picasso' library to asynchronously load a downloaded image into an 'ImageView'
                Picasso.with(this.getmContext()).load(this.getItemList().get(position - 1).getFilmImages().getFilmPoster()).into(((MyCardViewHolder) holder).icon_ImgView);
                // get element from the dataset at this position and replace the contents of the view with that element
                // The piece 'Html.fromHtml()' allows to deal with HTML 'CDATA' sections
                // 'position - 1' is employed so that the "date TextView" on top of the list is taken into account
                ((MyCardViewHolder) holder).title_TxtView.setText(this.getItemList().get(position - 1).getFilmTitle());
                ((MyCardViewHolder) holder).body_TxtView.setText(this.getItemList().get(position - 1).getFilmOverview());
                ((MyCardViewHolder) holder).release_TxtView.setText(Html.fromHtml("<i>Year: " + this.getItemList().get(position - 1).getFilmYear() + "</i>"));
            }
        }

        // Return the size of your data-set (invoked by the layout manager)
        // If the data-set is null, it returns 0 and 'onBindViewHolder' is never called
        // Once again, the size of the list is '+ 1' to take into account the "date TextView" on top
        @Override
        public int getItemCount() { return this.itemList == null ? 0 : this.itemList.size() + 1; }
    }
}
