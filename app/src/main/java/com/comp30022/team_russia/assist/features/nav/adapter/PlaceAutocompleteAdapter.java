package com.comp30022.team_russia.assist.features.nav.adapter;

/*
 * Copyright (C) 2015 Google Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/*
 * Modified from google maps example
 * https://github.com/googlesamples/android-play-places/tree/master/PlaceCompleteAdapter
 */

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.databinding.ViewPlacesearchDefaultBinding;
import com.comp30022.team_russia.assist.databinding.ViewPlacesearchFavouriteBinding;
import com.comp30022.team_russia.assist.databinding.ViewPlacesearchRecentBinding;
import com.comp30022.team_russia.assist.features.nav.models.PlaceSuggestionItem;
import com.comp30022.team_russia.assist.features.nav.models.PlaceSuggestionItemType;
import com.comp30022.team_russia.assist.features.nav.vm.NavigationViewModel;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.data.DataBufferUtils;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * Adapter that handles Autocomplete requests from the Places Geo Data API.
 * {@link AutocompletePrediction} results from the API are frozen and stored directly in this
 * adapter. (See {@link AutocompletePrediction#freeze()}.)
 *
 * <p>Note that this adapter requires a valid
 * {@link com.google.android.gms.common.api.GoogleApiClient}.
 * The API client must be maintained in the encapsulating Activity, including all lifecycle and
 * connection states. The API client must be connected with the {@link Places#GEO_DATA_API} API.
 */
public class PlaceAutocompleteAdapter
    extends ArrayAdapter<PlaceSuggestionItem> implements Filterable {

    /* variables */
    private static final String TAG = "PlaceAutocompleteAdapt";

    private static final CharacterStyle STYLE_BOLD = new StyleSpan(Typeface.BOLD);

    private final NavigationViewModel viewModel;

    /**
     * Current results returned by this adapter.
     */
    private ArrayList<PlaceSuggestionItem> resultList = new ArrayList<>();

    /**
     * Constructor. Initializes with a resource for text rows and autocomplete query bounds.
     *
     * @see android.widget.ArrayAdapter#ArrayAdapter(android.content.Context, int)
     */
    public PlaceAutocompleteAdapter(Context context, NavigationViewModel viewModel) {
        super(context, android.R.layout.simple_expandable_list_item_2, android.R.id.text1);
        this.viewModel = viewModel;
    }


    /* methods */
    /**
     * Returns the number of results received in the last autocomplete query.
     */
    @Override
    public int getCount() {
        return resultList.size();
    }

    /**
     * Returns an item from the last autocomplete query.
     */
    @Override
    public PlaceSuggestionItem getItem(int position) {
        return resultList.get(position);
    }

    /**
     * Updates the suggestions shown.
     * @param newSuggestions The new set of suggestions to be displayed.
     */
    public void setSuggestionList(List<PlaceSuggestionItem> newSuggestions) {
        // just clear and reload
        resultList.clear();
        if (newSuggestions != null && !newSuggestions.isEmpty()) {
            for (PlaceSuggestionItem suggestion : newSuggestions) {
                resultList.add(suggestion);
            }
            notifyDataSetChanged();
        } else {
            notifyDataSetInvalidated();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PlaceSuggestionItem item = getItem(position);
        LayoutInflater inflater = LayoutInflater.from(this.getContext());
        switch (item.getType()) {
        case NORMAL: {
            ViewPlacesearchDefaultBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.view_placesearch_default,
                parent, false);
            binding.setData(item);
            return binding.getRoot();
        }
        case RECENT: {
            ViewPlacesearchRecentBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.view_placesearch_recent,
                parent, false);
            binding.setData(item);
            return binding.getRoot();
        }
        case FAVOURITED: {
            ViewPlacesearchFavouriteBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.view_placesearch_favourite,
                parent, false);
            binding.setData(item);
            return binding.getRoot();
        }
        default:
            Log.e("", "Error creating view.");
            break;
        }
        return null;
    }

    /**
     * Returns the filter for the current set of autocomplete results.
     */
    @Override
    public Filter getFilter() {
        Log.d(TAG, "PlaceAutocompleteAdapter, getFilter called");
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();

                // We need a separate list to store the results, since
                // this is run asynchronously.
                List<PlaceSuggestionItem> filterData = new ArrayList<>();

                // Skip the autocomplete query if no constraints are given.
                if (constraint != null) {
                    Log.d(TAG, "PlaceAutocompleteAdapter, getFilter there is constraint");
                    // Query the autocomplete API for the (constraint) search string.
                    filterData = getAutocomplete(constraint);
                } else {
                    Log.d(TAG, "PlaceAutocompleteAdapter, getFilter there is NO constraint");
                }

                results.values = filterData;
                if (filterData != null) {
                    Log.d(TAG, "PlaceAutocompleteAdapter, getFilter there is filterData");
                    results.count = filterData.size();
                } else {
                    Log.d(TAG, "PlaceAutocompleteAdapter, getFilter, filterData is NULL");
                    results.count = 0;
                }

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    Log.d(TAG, "PlaceAutocompleteAdapter, getFilter, publishResults success");
                    setSuggestionList((List<PlaceSuggestionItem>) results.values);
                } else {
                    Log.d(TAG, "PlaceAutocompleteAdapter, getFilter, publishResults failure");
                    setSuggestionList(null);
                }
            }

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                // Override this method to display a readable result in the AutocompleteTextView
                // when clicked.
                if (resultValue instanceof PlaceSuggestionItem) {
                    Log.d(TAG,
                        "PlaceAutocompleteAdapter, getFilter, convertResultToString success");

                    // display this in the search box after result is clicked
                    return ((PlaceSuggestionItem) resultValue).getName() + " "
                           +  ((PlaceSuggestionItem) resultValue).getAddress();
                } else {
                    Log.d(TAG,
                        "PlaceAutocompleteAdapter, getFilter, convertResultToString failure");
                    return super.convertResultToString(resultValue);
                }
            }
        };
    }

    //CHECKSTYLE.OFF: ALL
    /**
     * Submits an autocomplete query to the Places Geo Data Autocomplete API.
     * Results are returned as frozen AutocompletePrediction objects, ready to be cached.
     * objects to store the Place ID and description that the API returns.
     * Returns an empty list if no results were found.
     * Returns null if the API client is not available or the query did not complete
     * successfully.
     * This method MUST be called off the main UI thread, as it will block until data is returned
     * from the API, which may include a network request.
     *
     * @param constraint Autocomplete query string
     * @return Results from the autocomplete API or null if the query was not successful.
     * @see Places#GEO_DATA_API#getAutocomplete(CharSequence)
     * @see AutocompletePrediction#freeze()
     */
    //CHECKSTYLE.ON: ALL
    private ArrayList<AutocompletePrediction> getAutocompleteGoogle(CharSequence constraint) {
        Log.e(TAG, "getAutocompleteGoogle started");

        GoogleApiClient googleApiClient = viewModel.getGoogleApiClient();

        Log.e(TAG, "getAutocompleteGoogle googleApiClient is: " + googleApiClient.toString());

        if (googleApiClient.isConnected()) {
            Log.e(TAG, "getAutocompleteGoogle is connected");
            Log.e(TAG, "Starting autocomplete query for: " + constraint);

            LatLngBounds currentLatLngBounds = getCurrentLatLngBounds();

            // Submit the query to the autocomplete API and retrieve a PendingResult that will
            // contain the results when the query completes.
            PendingResult<AutocompletePredictionBuffer> results =
                Places.GeoDataApi
                    .getAutocompletePredictions(googleApiClient, constraint.toString(),
                        currentLatLngBounds, null);

            // This method should have been called off the main UI thread.
            // Block and wait for at most 60s for a result from the API.
            AutocompletePredictionBuffer autocompletePredictions = results
                .await(60, TimeUnit.SECONDS);

            // Confirm that the query completed successfully, otherwise return null
            final Status status = autocompletePredictions.getStatus();
            if (!status.isSuccess()) {
                Log.e(TAG, "Error getting autocomplete prediction API call: " + status.toString());
                autocompletePredictions.release();
                return null;
            }

            Log.e(TAG, "Query completed. Received " + autocompletePredictions.getCount()
                        + " predictions.");

            // Freeze the results immutable representation that can be stored safely.
            return DataBufferUtils.freezeAndClose(autocompletePredictions);
        }
        Log.e(TAG, "getAutocompleteGoogle is NOT connected");
        Log.e(TAG, "Google API client is not connected for autocomplete query.");
        return null;
    }


    /**
     * Get autocomplete list from favourite and google autosuggestion.
     * @param constraint The search term.
     * @return List of suggestion items.
     */
    public List<PlaceSuggestionItem> getAutocomplete(CharSequence constraint) {
        Log.e(TAG, "getAutocomplete: started");
        List<AutocompletePrediction> googleAutoComplete = getAutocompleteGoogle(constraint);
        if (googleAutoComplete != null) {
            Log.e(TAG, "getAutocomplete: " + googleAutoComplete.toString());
        } else {
            Log.e(TAG, "getAutocomplete: googleAutoComplete is NULL");
        }

        ArrayList<PlaceSuggestionItem> result = new ArrayList<>();

        // add recents
        /*
        List<Recents> recents = viewModel.recentDestinations;
        for (Recents recent : recents) {
            result.add(new PlaceSuggestionItem(
                recent.getRecentName(),
                "",
                recent.getRecentPlaceId(),
                PlaceSuggestionItemType.RECENT));
        }
        */

        for (AutocompletePrediction prediction : googleAutoComplete) {
            result.add(new PlaceSuggestionItem(
                prediction.getPrimaryText(STYLE_BOLD).toString(),
                prediction.getSecondaryText(STYLE_BOLD).toString(),
                prediction.getPlaceId(),
                PlaceSuggestionItemType.NORMAL
            ));
        }
        return result;

    }


    /**
     * Get the boundary of search based on current location.
     * @return boundary of search
     */
    private LatLngBounds getCurrentLatLngBounds() {
        double radiusDegrees = 0.10;
        LatLng currentLocation = viewModel.currentApLocation.getValue();
        LatLng northEast = new LatLng(currentLocation.latitude + radiusDegrees,
            currentLocation.longitude + radiusDegrees);
        LatLng southWest = new LatLng(currentLocation.latitude - radiusDegrees,
            currentLocation.longitude - radiusDegrees);

        return LatLngBounds.builder().include(northEast).include(southWest).build();

    }


}