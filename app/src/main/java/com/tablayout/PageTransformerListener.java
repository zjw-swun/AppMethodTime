package com.tablayout;


interface PageTransformerListener {
    void setDirection(Direction direction);

    void setCurrentProgress(float currentProgress);

    void setVisibility(int visibility);

    enum Direction {
        LEFT_TO_RIGHT, RIGHT_TO_LIFT
    }

    /**
     * 离开
     *
     * @param leavePercent 离开的百分比, 0.0f - 1.0f
     * @param leftToRight  从左至右离开
     */
    void onLeave(int index, float leavePercent, boolean leftToRight);

    /**
     * 进入
     *
     * @param enterPercent 进入的百分比, 0.0f - 1.0f
     * @param leftToRight  从左至右离开
     */
    void onEnter(int index, float enterPercent, boolean leftToRight);
}
