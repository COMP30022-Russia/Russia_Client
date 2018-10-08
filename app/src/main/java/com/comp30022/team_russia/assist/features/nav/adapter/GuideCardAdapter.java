package com.comp30022.team_russia.assist.features.nav.adapter;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.features.nav.models.GuideCard;

import java.util.List;

/**
 * Guide Card Adapter.
 */
public class GuideCardAdapter extends
    RecyclerView.Adapter<GuideCardAdapter.ViewHolder> {


    /**
     * View Holder for Guide Card.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView transportModeIcon;
        public TextView distanceText;
        public TextView durationText;
        public TextView instructionsText;
        public TextView maneuverText;


        /**
         * Constructore for View Holder for guide card.
         * @param itemView view of each guide card item
         */
        public ViewHolder(View itemView) {
            super(itemView);
            transportModeIcon = itemView.findViewById(R.id.transport_mode_icon);
            distanceText = itemView.findViewById(R.id.distance_text);
            durationText = itemView.findViewById(R.id.duration_text);
            instructionsText = itemView.findViewById(R.id.instructions_text);
            maneuverText = itemView.findViewById(R.id.maneuver_text);

        }
    }


    private List<GuideCard> guideCards;

    public GuideCardAdapter(List<GuideCard> guideCards) {
        this.guideCards = guideCards;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View guideCardView = inflater.inflate(R.layout.item_guide_card_list, parent, false);

        ViewHolder viewHolder = new ViewHolder(guideCardView);
        return viewHolder;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

        GuideCard guideCard = this.guideCards.get(position);

        ImageView transportModeIcon = viewHolder.transportModeIcon;

        // parse transport mode to image
        if (!guideCard.getTravelMode().isEmpty()) {
            if (guideCard.getTravelMode().equals("WALKING")) {
                transportModeIcon.setImageResource(R.drawable.ic_walk_blue);

            } else if (guideCard.getTravelMode().equals("TRANSIT")) {
                transportModeIcon.setImageResource(R.drawable.ic_transit_blue);
            }
        }


        TextView distanceTextView = viewHolder.distanceText;
        distanceTextView.setText(guideCard.getDistance());

        TextView durationTextView = viewHolder.durationText;
        durationTextView.setText(guideCard.getDuration());

        TextView instructionsTextView = viewHolder.instructionsText;

        // parse html to plain text
        instructionsTextView.setText(Html.fromHtml(Html
            .fromHtml(guideCard.getInstructions()).toString()));

        TextView maneuverTextView = viewHolder.maneuverText;
        maneuverTextView.setText(guideCard.getManeuver());

    }

    @Override
    public int getItemCount() {
        return guideCards.size();
    }

}
