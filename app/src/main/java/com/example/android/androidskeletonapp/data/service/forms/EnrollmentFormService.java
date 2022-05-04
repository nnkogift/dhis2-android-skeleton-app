package com.example.android.androidskeletonapp.data.service.forms;

import android.text.TextUtils;

import com.example.android.androidskeletonapp.data.utils.Exercise;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.enrollment.EnrollmentCreateProjection;
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueObjectRepository;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Flowable;

public class EnrollmentFormService {

    private D2 d2;
    private EnrollmentObjectRepository enrollmentRepository;
    private static EnrollmentFormService instance;
    private final Map<String, FormField> fieldMap;

    private EnrollmentFormService() {
        fieldMap = new HashMap<>();
    }

    public static EnrollmentFormService getInstance() {
        if (instance == null)
            instance = new EnrollmentFormService();

        return instance;
    }

    @Exercise(
            exerciseNumber = "ex09b-trackerDataCreation",
            title = "Enrollment Creation",
            tips = "Create an enrollment, store the enrollment uid and set enrollment and incident date"
    )
    public boolean init(D2 d2, String teiUid, String programUid, String ouUid) {
        this.d2 = d2;
        // TODO Create a new enrollment and save the enrollment uid in the class variable 'enrollmentUid'

        // TODO Set enrollmentDate and incidentDate. Tip: use helper method 'getNowWithoutTime()'

        return false;
    }


    public Flowable<Map<String, FormField>> getEnrollmentFormFields() {
        if (d2 == null)
            return Flowable.error(
                    new NullPointerException("D2 is null. EnrollmentForm has not been initialized, use init() function.")
            );
        else
            return Flowable.fromCallable(() ->
                    d2.programModule().programTrackedEntityAttributes()
                            .byProgram().eq(enrollmentRepository.blockingGet().program()).blockingGet()
            )
                    .flatMapIterable(programTrackedEntityAttributes -> programTrackedEntityAttributes)
                    .map(programAttribute -> {

                        TrackedEntityAttribute attribute = d2.trackedEntityModule().trackedEntityAttributes()
                                .uid(programAttribute.trackedEntityAttribute().uid())
                                .blockingGet();

                        String initialValue = getInitialValue(attribute);

                        FormField field = new FormField(
                                attribute.uid(),
                                attribute.optionSet() != null ? attribute.optionSet().uid() : null,
                                attribute.valueType(),
                                String.format("%s%s", attribute.formName(), programAttribute.mandatory() ? "*" : ""),
                                initialValue,
                                null,
                                !attribute.generated() || initialValue == null,
                                attribute.style()
                        );


                        fieldMap.put(programAttribute.trackedEntityAttribute().uid(), field);
                        return programAttribute;
                    }).toList().toFlowable()
                    .map(list -> fieldMap);
    }

    @Exercise(
            exerciseNumber = "ex09d-trackerDataCreation",
            title = "Auto generated attributes",
            tips = "If the attribute is 'generated', get a reserved value, store it in the database and return it;" +
                    "otherwise return null"
    )
    private String getInitialValue(TrackedEntityAttribute attribute) {
        // TODO If the attribute is 'generated', get a reserved value, store it in the database and return it;
        //      otherwise return null

        return null;
    }

    public String getEnrollmentUid() {
        return enrollmentRepository.blockingGet().uid();
    }

    public void delete() {
        try {
            enrollmentRepository.blockingDelete();
        } catch (D2Error d2Error) {
            d2Error.printStackTrace();
        }
    }

    public static void clear() {
        instance = null;
    }

    private Date getNowWithoutTime() {
        final GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(new Date());
        gc.set(Calendar.HOUR_OF_DAY, 0);
        gc.set(Calendar.MINUTE, 0);
        gc.set(Calendar.SECOND, 0);
        gc.set(Calendar.MILLISECOND, 0);
        return gc.getTime();
    }

}
