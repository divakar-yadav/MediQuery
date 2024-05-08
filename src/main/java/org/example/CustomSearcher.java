package org.example;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.util.BytesRef;
import java.io.IOException;

public class CustomSearcher extends IndexSearcher {
    public CustomSearcher(IndexReader reader) {
        super(reader);
        setSimilarity(new CustomSimilarity()); // Use custom similarity incorporating citation count and published year.
    }

    @Override
    public Weight createWeight(Query query, ScoreMode scoreMode, float boost) throws IOException {
        Weight originalWeight = super.createWeight(query, scoreMode, boost);
        return new CustomWeight(originalWeight);
    }

    private class CustomWeight extends Weight {
        private final Weight originalWeight;

        public CustomWeight(Weight originalWeight) {
            super(originalWeight.getQuery());
            this.originalWeight = originalWeight;
        }

        @Override
        public Scorer scorer(LeafReaderContext context) throws IOException {
            Scorer originalScorer = originalWeight.scorer(context);
            if (originalScorer == null) return null;
            NumericDocValues citationCounts = context.reader().getNumericDocValues("citation_count");
            NumericDocValues publishedYears = context.reader().getNumericDocValues("published_year");
            return new CustomScorer(originalScorer, citationCounts, publishedYears);
        }

        @Override
        public boolean isCacheable(LeafReaderContext ctx) {
            return originalWeight.isCacheable(ctx);
        }

        @Override
        public Explanation explain(LeafReaderContext context, int doc) throws IOException {
            Explanation originalExp = originalWeight.explain(context, doc);
            NumericDocValues citationCounts = context.reader().getNumericDocValues("citation_count");
            NumericDocValues publishedYears = context.reader().getNumericDocValues("published_year");
            if (citationCounts != null && citationCounts.advanceExact(doc) &&
                    publishedYears != null && publishedYears.advanceExact(doc)) {
                long citationCount = citationCounts.longValue();
                long publishedYear = publishedYears.longValue();
                float citationScore = refineCitationInfluence(citationCount);
                float yearScore = calculateYearScore(publishedYear);
                float combinedScore = originalExp.getValue().floatValue() * (1 + citationScore) * yearScore;
                return Explanation.match(combinedScore, "custom score: ", originalExp);
            }
            return originalExp;
        }
    }

    private class CustomScorer extends FilterScorer {
        private final NumericDocValues citationCounts;
        private final NumericDocValues publishedYears;

        protected CustomScorer(Scorer originalScorer, NumericDocValues citationCounts, NumericDocValues publishedYears) {
            super(originalScorer);
            this.citationCounts = citationCounts;
            this.publishedYears = publishedYears;
        }

        @Override
        public float score() throws IOException {
            int doc = docID();
            if (citationCounts.advanceExact(doc) && publishedYears.advanceExact(doc)) {
                long citationCount = citationCounts.longValue();
                long publishedYear = publishedYears.longValue();
                float citationScore = refineCitationInfluence(citationCount);
                float yearScore = calculateYearScore(publishedYear);
                return super.score() * (1 + citationScore) * yearScore;
            }
            return super.score();
        }

        @Override
        public float getMaxScore(int i) throws IOException {
            return 0;
        }
    }

    // Custom similarity incorporating citation count and published year.
    private class CustomSimilarity extends BM25Similarity {


        public float scorePayload(int doc, int start, int end, BytesRef payload) {
            return 1.0f;
        }
    }

    private float refineCitationInfluence(long citationCount) {
        // Adjust this function as needed to refine the influence of citation count.
        // You can experiment with different functions or introduce a threshold.
        // For example, you could use a sigmoid function or a threshold-based approach.
        return (float) Math.log1p(citationCount); // Default logarithmic function
    }

    private float calculateYearScore(long publishedYear) {
        // Adjust this function as needed to give higher score to recent documents.
        // For example, you can use a Gaussian function or a linear function based on the difference from current year.
        int currentYear = 2023; // Update this with the current year
        float yearDifference = currentYear - publishedYear;
        return 1.0f / (1 + yearDifference); // Linear function giving higher score to recent documents.
    }
}
