import api from '@/plugins/axios'
import { ADMIN } from '@/utils/endpoints'
import type {
    ObjectResponse,
    ObjectListResponse,
    MeasurementSourceType,
    Laboratories,
    Resolutions,
    ResolutionBySubSources,
    SubsourceTypes,
    SourceBySubsource,
    ResolutionBySubSourcesRequest,
    SourceBySubsourceRequest,
    NormSectors,
    ResolutionsByNormSectors,
    ResolutionsByNormSectorsRequest,
    TypeActivity,
    TypeActivityByNormSectors,
    TypeActivityByNormSectorsRequest,
    MeasurementParameters,
    MeasurementParametersRequest,
} from '@/types'
import type { AxiosResponse } from 'axios'

const getMeasurementSources = (
    queryAll: boolean = true,
): Promise<AxiosResponse<ObjectListResponse<MeasurementSourceType>>> =>
    api.get<ObjectListResponse<MeasurementSourceType>>(ADMIN.GET_MEASUREMENT_SOURCES, {
        params: { queryAll },
    })

const postInsertOrReplaceMeasurementSources = (
    payload: MeasurementSourceType,
): Promise<AxiosResponse<ObjectResponse<MeasurementSourceType>>> =>
    api.post<ObjectResponse<MeasurementSourceType>>(
        ADMIN.POST_INSERT_OR_REPLACE_MEASUREMENT_SOURCES,
        payload,
    )

const getLaboratories = (): Promise<AxiosResponse<ObjectListResponse<Laboratories>>> =>
    api.get<ObjectListResponse<Laboratories>>(ADMIN.GET_LABORATORIES)

const postSyncLaboratories = (): Promise<AxiosResponse<ObjectResponse<void>>> =>
    api.post<ObjectResponse<void>>(ADMIN.POST_SYNC_LABORATORIES, {})

const getResolutions = (
    queryAll: boolean = true,
): Promise<AxiosResponse<ObjectListResponse<Resolutions>>> =>
    api.get<ObjectListResponse<Resolutions>>(ADMIN.GET_RESOLUTIONS, { params: { queryAll } })

const postInsertOrReplaceResolutions = (
    payload: Resolutions,
): Promise<AxiosResponse<ObjectResponse<void>>> =>
    api.post<ObjectResponse<void>>(ADMIN.POST_INSERT_OR_REPLACE_RESOLUTIONS, payload)

const getResolutionBySubSource = (
    idResolution: number,
    queryAll: boolean = true,
): Promise<AxiosResponse<ObjectListResponse<ResolutionBySubSources>>> =>
    api.get<ObjectListResponse<ResolutionBySubSources>>(ADMIN.GET_RESOLUTION_BY_SUBSOURCE, {
        params: { queryAll, idResolution },
    })

const postInsertOrReplaceResolutionBySubSources = (
    payload: ResolutionBySubSourcesRequest,
): Promise<AxiosResponse<ObjectResponse<ResolutionBySubSourcesRequest>>> =>
    api.post<ObjectResponse<ResolutionBySubSourcesRequest>>(
        ADMIN.POST_INSERT_OR_REPLACE_RESOLUTION_BY_SUBSOURCES,
        payload,
    )

const getSubsourceTypes = (
    queryAll: boolean = true,
): Promise<AxiosResponse<ObjectListResponse<SubsourceTypes>>> =>
    api.get<ObjectListResponse<SubsourceTypes>>(ADMIN.GET_MEASUREMENT_SUBSOURCES, {
        params: { queryAll },
    })

const postInsertOrReplaceSubsourceTypes = (
    payload: SubsourceTypes,
): Promise<AxiosResponse<ObjectResponse<void>>> =>
    api.post<ObjectResponse<void>>(ADMIN.POST_INSERT_OR_REPLACE_MEASUREMENT_SUBSOURCES, payload)

const getSourceBySubsource = (
    idSubSource: number,
    queryAll: boolean = true,
): Promise<AxiosResponse<ObjectListResponse<SourceBySubsource>>> =>
    api.get<ObjectListResponse<SourceBySubsource>>(ADMIN.GET_SOURCES_BY_SUBSOURCES, {
        params: { queryAll, idSubSource },
    })

const postInsertOrReplaceSourceBySubsource = (
    payload: SourceBySubsourceRequest,
): Promise<AxiosResponse<ObjectResponse<void>>> =>
    api.post<ObjectResponse<void>>(ADMIN.POST_INSERT_OR_REPLACE_SOURCES_BY_SUBSOURCES, payload)

const getNormSectors = (
    queryAll: boolean = true,
): Promise<AxiosResponse<ObjectListResponse<NormSectors>>> =>
    api.get<ObjectListResponse<NormSectors>>(ADMIN.GET_NORMSECTORS, { params: { queryAll } })

const postInsertOrReplaceNormSectors = (
    payload: NormSectors,
): Promise<AxiosResponse<ObjectResponse<void>>> =>
    api.post<ObjectResponse<void>>(ADMIN.POST_INSERT_OR_REPLACE_NORMSECTORS, payload)

const getResolutionsByNormSectors = (
    idNormativeSector: number,
    queryAll: boolean = true,
): Promise<AxiosResponse<ObjectListResponse<ResolutionsByNormSectors>>> =>
    api.get<ObjectListResponse<ResolutionsByNormSectors>>(ADMIN.GET_RESOLUTIONS_BY_NORMSECTORS, {
        params: { queryAll, idNormativeSector },
    })

const postInsertOrReplaceResolutionsByNormSectors = (
    payload: ResolutionsByNormSectorsRequest,
): Promise<AxiosResponse<ObjectResponse<void>>> =>
    api.post<ObjectResponse<void>>(ADMIN.POST_INSERT_OR_REPLACE_RESOLUTIONS_BY_NORMSECTORS, payload)

const getTypeActivity = (
    queryAll: boolean = true,
): Promise<AxiosResponse<ObjectListResponse<TypeActivity>>> =>
    api.get<ObjectListResponse<TypeActivity>>(ADMIN.GET_TYPE_ACTIVITY, { params: { queryAll } })

const postInsertOrReplaceTypeActivity = (
    payload: TypeActivity,
): Promise<AxiosResponse<ObjectResponse<void>>> =>
    api.post<ObjectResponse<void>>(ADMIN.POST_INSERT_OR_REPLACE_TYPE_ACTIVITY, payload)

const getTypeActivityByNormSectors = (
    idTypeActivity: number,
    queryAll: boolean = true,
): Promise<AxiosResponse<ObjectListResponse<TypeActivityByNormSectors>>> =>
    api.get<ObjectListResponse<TypeActivityByNormSectors>>(ADMIN.GET_TYPE_ACTIVITY_BY_NORMSECTORS, {
        params: { queryAll, idTypeActivity },
    })

const postInsertOrReplaceTypeActivityByNormSectors = (
    payload: TypeActivityByNormSectorsRequest,
): Promise<AxiosResponse<ObjectResponse<void>>> =>
    api.post<ObjectResponse<void>>(
        ADMIN.POST_INSERT_OR_REPLACE_TYPE_ACTIVITY_BY_NORMSECTORS,
        payload,
    )
const getMeasurementParameters = (
    queryAll: boolean = true,
): Promise<AxiosResponse<ObjectListResponse<MeasurementParameters>>> =>
    api.get<ObjectListResponse<MeasurementParameters>>(`${ADMIN.GET_MEASUREMENT_PARAMETERS}`, {
        params: { queryAll },
    })

const postInsertOrReplaceMeasurementParameters = (
    payload: MeasurementParametersRequest,
): Promise<AxiosResponse<ObjectResponse<void>>> =>
    api.post<ObjectResponse<void>>(ADMIN.POST_INSERT_OR_REPLACE_MEASUREMENT_PARAMETER, payload)

export const adminService = {
    getMeasurementSources,
    postInsertOrReplaceMeasurementSources,
    getLaboratories,
    postSyncLaboratories,
    getResolutions,
    postInsertOrReplaceResolutions,
    getResolutionBySubSource,
    postInsertOrReplaceResolutionBySubSources,
    getSubsourceTypes,
    postInsertOrReplaceSubsourceTypes,
    getSourceBySubsource,
    postInsertOrReplaceSourceBySubsource,
    getNormSectors,
    postInsertOrReplaceNormSectors,
    getResolutionsByNormSectors,
    postInsertOrReplaceResolutionsByNormSectors,
    getTypeActivity,
    postInsertOrReplaceTypeActivity,
    getTypeActivityByNormSectors,
    postInsertOrReplaceTypeActivityByNormSectors,
    getMeasurementParameters,
    postInsertOrReplaceMeasurementParameters,
}
