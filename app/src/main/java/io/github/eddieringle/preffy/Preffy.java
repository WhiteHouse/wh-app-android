package io.github.eddieringle.preffy;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Set;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

public abstract
class Preffy<T>
{
    final
    BehaviorSubject<T> mSubject;

    final
    SharedPreferences mPrefs;

    final
    String mKey;

    final
    T mDefault;

    public static
    Preffy<Boolean> initBoolPref(Context ctx, String pref, boolean defValue)
    {
        return new Preffy<Boolean>(ctx, pref, defValue)
        {
            @Override
            public
            void doSetValue(Boolean value)
            {
                mPrefs.edit().putBoolean(mKey, value).apply();
            }

            @Override
            public
            Boolean getValue()
            {
                return mPrefs.getBoolean(mKey, mDefault);
            }
        };
    }

    public static
    Preffy<Float> initFloatPref(Context ctx, String pref, float defValue)
    {
        return new Preffy<Float>(ctx, pref, defValue)
        {
            @Override
            public
            void doSetValue(Float value)
            {
                mPrefs.edit().putFloat(mKey, value).apply();
            }

            @Override
            public
            Float getValue()
            {
                return mPrefs.getFloat(mKey, mDefault);
            }
        };
    }

    public static
    Preffy<Integer> initIntPref(Context ctx, String pref, int defValue)
    {
        return new Preffy<Integer>(ctx, pref, defValue)
        {
            @Override
            public
            void doSetValue(Integer value)
            {
                mPrefs.edit().putInt(mKey, value).apply();
            }

            @Override
            public
            Integer getValue()
            {
                return mPrefs.getInt(mKey, mDefault);
            }
        };
    }

    public static
    Preffy<Long> initLongPref(Context ctx, String pref, long defValue)
    {
        return new Preffy<Long>(ctx, pref, defValue)
        {
            @Override
            public
            void doSetValue(Long value)
            {
                mPrefs.edit().putLong(mKey, value).apply();
            }

            @Override
            public
            Long getValue()
            {
                return mPrefs.getLong(mKey, mDefault);
            }
        };
    }

    public static
    Preffy<String> initStringPref(Context ctx, String pref, String defValue)
    {
        return new Preffy<String>(ctx, pref, defValue)
        {
            @Override
            public
            void doSetValue(String value)
            {
                mPrefs.edit().putString(mKey, value).apply();
            }

            @Override
            public
            String getValue()
            {
                return mPrefs.getString(mKey, mDefault);
            }
        };
    }

    public static
    Preffy<Set<String>> initStringSetPref(Context ctx, String pref, Set<String> defValue)
    {
        return new Preffy<Set<String>>(ctx, pref, defValue)
        {
            @Override
            public
            void doSetValue(Set<String> value)
            {
                mPrefs.edit().putStringSet(mKey, value).apply();
            }

            @Override
            public
            Set<String> getValue()
            {
                return mPrefs.getStringSet(mKey, mDefault);
            }
        };
    }

    abstract
    void doSetValue(T value);

    public abstract
    T getValue();

    private
    Preffy(Context ctx, String pref, T defValue)
    {
        mKey = pref;
        mDefault = defValue;
        mPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        mSubject = BehaviorSubject.create(defValue);
        mSubject.subscribeOn(Schedulers.io());
        if (!mPrefs.contains(pref)) {
            doSetValue(defValue);
        }
    }

    public
    void setValue(T value)
    {
        doSetValue(value);
        mSubject.onNext(value);
    }

    public
    Observable<T> watch()
    {
        return mSubject.asObservable().observeOn(AndroidSchedulers.mainThread());
    }
}
