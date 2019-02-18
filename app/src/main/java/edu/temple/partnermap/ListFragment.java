package edu.temple.partnermap;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


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

        //initDataset();
        //makeAndAssignPartnerData();
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
     * Generates Strings for RecyclerView's adapter. This data would usually come
     * from a local content provider or remote server.
     */
    private void initDataset() {
        mDataset = new String[DATASET_COUNT];
        for (int i = 0; i < DATASET_COUNT; i++) {
            mDataset[i] = "This is element #" + i;
        }
    }


    /**
     *  This is where I work on sorting the list of partners
     *  Trust me this stuff won't be in the same shape when I present it
     */

    Partner[] partners;

    public void makeAndAssignPartnerData(Partner[] thePartners) {
        makePartnerDataset(thePartners);
        partnerBubbleSort();
        convertPartnerArray();
    }

    private void makePartnerDataset(Partner[] thePartners) {
        partners = thePartners; /*new Partner[5];
        partners[0] = new Partner("steve top", 123, 123, 5);
        partners[1] = new Partner("steve bot", 123, 123, 9);
        partners[2] = new Partner("steve mid", 123, 123, 7);
        partners[3] = new Partner("bob high", 123, 123, 6);
        partners[4] = new Partner("bob low", 123, 123, 8); */
    }

    private void partnerBubbleSort() {
        int n = partners.length;

        for (int i = 0; i < n-1; i++)
            for (int j = 0; j < n-i-1; j++)
                if (partners[j].compareTo(partners[j+1]) > 0)
                {
                    // swap arr[j+1] and arr[i]
                    Partner temp = partners[j];
                    partners[j] = partners[j+1];
                    partners[j+1] = temp;
                }
    }

    private void convertPartnerArray() {
        mDataset = new String[partners.length];
        for (int i = 0; i < partners.length; i++) {
            mDataset[i] = partners[i].toString();
        }
    }
}

