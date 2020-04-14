package com.example.noiseconstruct.module;

public class modulus {
    public float modulus(float[] var1, int var2) {
        int var3 = 2 * var2;
        int var4 = 2 * var2 + 1;
        float var5 = var1[var3] * var1[var3] + var1[var4] * var1[var4];
        return (float)Math.sqrt((double)var5);
    }

    public void modulus(float[] var1, float[] var2) {
        assert var1.length / 2 == var2.length;

        for(int var3 = 0; var3 < var2.length; ++var3) {
            var2[var3] = this.modulus(var1, var3);
        }
    }
}
