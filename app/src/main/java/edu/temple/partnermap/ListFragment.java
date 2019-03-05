package edu.temple.partnermap;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ListFragment extends Fragment {

    private static final int DATASET_COUNT = 10;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    protected String[] mDataset;

    public ListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_list, container, false);

        mRecyclerView = v.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true); // optional, helps with sizing?

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new MyAdapter(mDataset);
        mRecyclerView.setAdapter(mAdapter);

        return v;
    }


    /**
     *  This is where I work on sorting the list of partners
     *  Trust me this stuff won't be in the same shape when I present it
     */

    List<Partner> partners;
    int length;

    public void makeAndAssignPartnerData(List<Partner> thePartners) {
        if (thePartners.toArray() != null)
            length = thePartners.toArray().length;
        else
            length = 0;
        makePartnerDataset(thePartners);
        partnerBubbleSort();
        convertPartnerArray();
    }

    private void makePartnerDataset(List<Partner> thePartners) {
        partners = thePartners;
    }

    private void partnerBubbleSort() {
        int n = length;

        for (int i = 0; i < n-1; i++)
            for (int j = 0; j < n-i-1; j++)
                if (partners.get(j).compareTo(partners.get(j+1)) < 0)
                {
                    // swap arr[j+1] and arr[i]
                    Partner temp = partners.get(j);
                    partners.set(j,partners.get(j+1));
                    partners.set(j+1, temp);
                }
    }

    private void convertPartnerArray() {
        mDataset = new String[length];
        for (int i = 0; i < length; i++) {
            mDataset[i] = partners.get(i).toString();
        }
    }
}

