package com.rngad33.yxpei.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 算法工具类
 */
public class AlgorithmUtils {

    /**
     * 综合相似度计算（结合编辑距离和Jaccard相似度）
     *
     * @param tagList1 第一组标签
     * @param tagList2 第二组标签
     * @return 综合相似度值（0-1之间，1表示最相似）
     */
    public static double comprehensiveSimilarity(List<String> tagList1, List<String> tagList2) {
        // 归一化编辑距离（转换为相似度）
        double editSimilarity = 1.0 - normalizedDistance(tagList1, tagList2);

        // Jaccard相似度
        double jaccardSim = jaccardSimilarity(tagList1, tagList2);

        // 加权平均（可以根据实际效果调整权重）
        return 0.4 * editSimilarity + 0.6 * jaccardSim;
    }

    /**
     * 计算归一化的编辑距离相似度（值越小越相似）
     *
     * @param tagList1 第一组标签
     * @param tagList2 第二组标签
     * @return 归一化的相似度值（0-1之间，0表示完全相同）
     */
    public static double normalizedDistance(List<String> tagList1, List<String> tagList2) {
        if (tagList1.isEmpty() && tagList2.isEmpty()) {
            return 0.0;
        }

        int distance = minDistance(tagList1, tagList2);
        int maxLength = Math.max(tagList1.size(), tagList2.size());

        return (double) distance / maxLength;
    }

    /**
     * 计算Jaccard相似度
     *
     * @param tagList1 第一组标签
     * * @param tagList2 第二组标签
     * @return Jaccard相似度（0-1之间，1表示完全相同）
     */
    public static double jaccardSimilarity(List<String> tagList1, List<String> tagList2) {
        if (tagList1.isEmpty() && tagList2.isEmpty()) {
            return 1.0;
        }

        Set<String> set1 = new HashSet<>(tagList1);
        Set<String> set2 = new HashSet<>(tagList2);

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        return (double) intersection.size() / union.size();
    }

    /**
     * 编辑距离算法（用于计算最相似的两组标签）
     *
     * @url https://blog.csdn.net/DBC_121/article/details/104198838
     * @param tagList1
     * @param tagList2
     * @return
     */
    public static int minDistance(List<String> tagList1, List<String> tagList2) {
        int n = tagList1.size();
        int m = tagList2.size();

        if (n * m == 0) {
            return n + m;
        }

        int[][] d = new int[n + 1][m + 1];
        for (int i = 0; i < n + 1; i++) {
            d[i][0] = i;
        }

        for (int j = 0; j < m + 1; j++) {
            d[0][j] = j;
        }

        for (int i = 1; i < n + 1; i++) {
            for (int j = 1; j < m + 1; j++) {
                int left = d[i - 1][j] + 1;
                int down = d[i][j - 1] + 1;
                int left_down = d[i - 1][j - 1];
                if (!Objects.equals(tagList1.get(i - 1), tagList2.get(j - 1))) {
                    left_down += 1;
                }
                d[i][j] = Math.min(left, Math.min(down, left_down));
            }
        }
        return d[n][m];
    }

    /**
     * 编辑距离算法（用于计算最相似的两个字符串）
     *
     * @url https://blog.csdn.net/DBC_121/article/details/104198838
     * @param word1
     * @param word2
     * @return
     */
    public static int minDistance(String word1, String word2) {
        int n = word1.length();
        int m = word2.length();

        if (n * m == 0) {
            return n + m;
        }

        int[][] d = new int[n + 1][m + 1];
        for (int i = 0; i < n + 1; i++) {
            d[i][0] = i;
        }

        for (int j = 0; j < m + 1; j++) {
            d[0][j] = j;
        }

        for (int i = 1; i < n + 1; i++) {
            for (int j = 1; j < m + 1; j++) {
                int left = d[i - 1][j] + 1;
                int down = d[i][j - 1] + 1;
                int left_down = d[i - 1][j - 1];
                if (word1.charAt(i - 1) != word2.charAt(j - 1)) {
                    left_down += 1;
                }
                d[i][j] = Math.min(left, Math.min(down, left_down));
            }
        }
        return d[n][m];
    }

}