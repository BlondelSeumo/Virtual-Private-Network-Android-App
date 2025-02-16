import Layout from "@app/components/layout";
import React from "react";
import {Switch, notification, Select} from "antd";
import UIButton from "@app/components/core/button";
import UITable from "@app/components/core/table";
import {auth, firestore} from "@app/services/firebase";
import {EMAIL} from "@app/configs";
import {runFunction} from "@app/services/casl/ability";
import {v4} from "uuid";

const staticData = [
  {
    os: "android",
    id: "android",
    banner: "11",
    show: "133"
  },
  {
    os: "ios",
    id: "ios",
    banner: "11",
    show: "133"
  }
]

export default () => {
  const [filter, setFilter] = React.useState({})
  const [configAds, setConfigs] = React.useState({})
  const [ads, setAds] = React.useState([])
  const [userInfo, setUserInfo] = React.useState(null)
  const [isReloadTable, setReloadTable] = React.useState("")
  const [changeValue, setChangeValue] = React.useState({
    id: undefined,
    value: undefined
  })
  const [isShowAddBoost, setShowAddBoost] = React.useState({
    status: false,
    data: undefined
  })

  const onUpdateAds = () => {
    if (changeValue && changeValue?.id) {
      const oldAds = {
        show: changeValue?.form?.show,
        banner: changeValue?.form?.banner,
      }

      firestore.collection(`configs`).doc(changeValue?.form?.rawId).update({
        ...configAds,
        ads: [
          {
            google: {
              ...oldAds,
              [changeValue?.type]: changeValue?.value
            }
          }
        ]
      }).then((result) => {
        notification.info({
          description: `Ads was updated successfully`,
          placement: "bottomRight",
          duration: 2,
          icon: "",
          className: "core-notification info",
        });
        setChangeValue({
          id: undefined,
          value: undefined
        })
        getAds()
      })
        .catch((err) => {
          console.log(err)
          notification.info({
            description: `Ads was updated failure`,
            placement: "bottomRight",
            duration: 2,
            icon: "",
            className: "core-notification error",
            onClose: () => getAds()
          });
        })
    }
  }

  React.useEffect(() => {
    auth.onAuthStateChanged((user) => {
      if (user) {
        setUserInfo(user)
        getAds()
      }
    })
  }, [])

  const getAds = () => {
    firestore.collection("configs").get()
      .then(({docs}) => {
        setConfigs(docs?.[0]?.data?.())
        handleAdsData(docs?.[0]?.data?.()?.ads, docs?.[0]?.id)
      })
      .catch()
  }

  const handleAdsData = (rawAds = [], rawId) => {

    const newAds = rawAds?.reduce((arr, ad) => ([...arr,
      ...(Object.entries(ad).reduce((subArr, item) => [...subArr, {
        platform: item?.[0],
        banner: item?.[1]?.banner,
        show: item?.[1]?.show,
        enable: item?.[1]?.enable || true,
        id: v4(),
        rawId
      }], []))
    ]), [])

    setAds(newAds)
  }

  const currentUser = auth?.currentUser
  const isAllow = (currentUser?.email && currentUser?.email?.toLowerCase() !== EMAIL) || (userInfo?.email && userInfo?.email !== EMAIL)

  return (
    <Layout title="Ads">
      <div className="core-card">
        <div className="flex justify-between mb-4">
          {
            changeValue?.id && isAllow && (
              <>
                <UIButton onClick={() => {
                  setChangeValue({
                    id: undefined,
                    value: undefined
                  })
                  getAds()
                }} className="border mr-2">Cancel</UIButton>
                <UIButton onClick={() => onUpdateAds()} className="secondary">Save</UIButton>
              </>
            )
          }
        </div>
        <UITable
          isReload={isReloadTable}
          customComp={{
            platform: ({text}) => (
              <div className="text-center">
                {text}
              </div>
            ),
            adsType: () => (
              <div className="text-center">
                Show
              </div>
            ),
            banner: ({text, row}) => (
              <div className="text-left">
                <input
                  onChange={({target: {value}}) => isAllow &&
                    setChangeValue({
                      value,
                      id: `${row?.id}-banner`,
                      form: row,
                      type: 'banner'
                    })
                  }
                  value={changeValue?.id === `${row?.id}-banner` ? changeValue?.value : text}
                  placeholder="Banner"
                  className="w-full outline-none px-2 py-1"/>
              </div>
            ),
            show: ({text, row}) => (
              <div className="text-left">
                <input
                  onChange={({target: {value}}) => isAllow &&
                    setChangeValue({
                      value,
                      id: `${row?.id}-show`,
                      form: row,
                      type: 'show'
                    })
                  }
                  value={changeValue?.id === `${row?.id}-show` ? changeValue?.value : text}
                  placeholder="Show"
                  className="w-full outline-none px-2 py-1"/>
              </div>
            ),
            action: ({row}) => (
              <span>
                <Switch disabled onChange={(enable) => {
                  runFunction(() => {
                    onUpdateAds({
                      "id": row?._id,
                      "adsId": row?.adsId,
                      enable
                    })
                  })
                }} defaultChecked={row?.enable}/>
              </span>
            ),
          }}
          staticData={ads}
          isHiddenPg={false}
          defineCols={[
            {
              name: () => (
                <div className="text-left">
                  <span>Banner</span>
                </div>
              ),
              code: "banner",
              sort: 1
            },
            {
              name: () => (
                <div className="text-left">
                  <span>Show</span>
                </div>
              ),
              code: "show",
              sort: 1
            },
            {
              name: () => (
                <div className="text-center">
                  <span>Ads Provider</span>
                </div>
              ),
              code: "platform",
              sort: 2
            },
            {
              name: () => <div className="text-center">Ads Type</div>,
              code: "adsType",
              sort: 3
            },
            {
              name: () => <div className="text-center">Action</div>,
              code: "action",
              sort: 'end'
            }
          ]}
          payload={filter}
          headerWidth={{
            action: 92,
            adsName: 989,
            adsId: 215,
          }}
          columns={[]}
        />
      </div>
    </Layout>
  )
}