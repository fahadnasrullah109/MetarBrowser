package com.metar.browser.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.metar.browser.R;
import com.metar.browser.database.MetarEntity;
import com.metar.browser.databinding.SuggestionItemBinding;
import com.metar.browser.interfaces.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

public class SuggestionRecyclerAdapter extends RecyclerView.Adapter<SuggestionRecyclerAdapter.ViewHolder> implements Filterable {
    private List<MetarEntity> mItemsList, mFilteredList;
    private OnItemClickListener onItemClickListener;
    private ValueFilter mValueFilter;

    public SuggestionRecyclerAdapter(List<MetarEntity> list, OnItemClickListener onItemClickListener) {
        this.mItemsList = this.mFilteredList = list;
        this.onItemClickListener = onItemClickListener;
    }

    public void setList(List<MetarEntity> list) {
        this.mItemsList = list;
        notifyDataSetChanged();
    }

    @Override
    public SuggestionRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        SuggestionItemBinding itemBinding =
                DataBindingUtil.inflate(inflater, R.layout.suggestion_item, parent, false);
        return new ViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionRecyclerAdapter.ViewHolder holder, int position) {
        holder.bind(mFilteredList.get(position));
    }

    @Override
    public int getItemCount() {
        return mFilteredList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final SuggestionItemBinding mBinding;

        ViewHolder(SuggestionItemBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
            mBinding.itemTitleTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null && getAdapterPosition() != -1) {
                        onItemClickListener.onItemClick(getAdapterPosition());
                    }
                }
            });
        }

        public void bind(MetarEntity obj) {
            mBinding.setMessageObj(obj);
            mBinding.executePendingBindings();
        }
    }

    private class ValueFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint != null && constraint.length() > 0) {
                List<MetarEntity> filterList = new ArrayList<>();
                for (int i = 0; i < mItemsList.size(); i++) {
                    if ((mItemsList.get(i).getStation().toUpperCase()).contains(constraint.toString().toUpperCase())) {
                        filterList.add(mItemsList.get(i));
                    }
                }
                results.count = filterList.size();
                results.values = filterList;
            } else {
                results.count = mItemsList.size();
                results.values = mItemsList;
            }
            return results;

        }

        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            mFilteredList = (List<MetarEntity>) results.values;
            notifyDataSetChanged();
        }

    }

    @Override
    public Filter getFilter() {
        if (mValueFilter == null) {
            mValueFilter = new ValueFilter();
        }
        return mValueFilter;
    }
}