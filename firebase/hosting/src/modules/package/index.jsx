import Layout from "@app/components/layout";
import React from "react";
import { notification } from "antd";
import UIButton from "@app/components/core/button";
import UITable from "@app/components/core/table";
import {auth, firestore} from "@app/services/firebase";
import {EMAIL} from "@app/configs";
import {v4} from "uuid";

export default () => {
  const [configPacks, setConfigPacks] = React.useState({})
  const [packs, setPack] = React.useState([])
  const [userInfo, setUserInfo] = React.useState(null)
  const [changeValue, setChangeValue] = React.useState({
    id: undefined,
    value: undefined
  })

  const onUpdateAds = () => {
    if (changeValue && changeValue?.id) {
      firestore.collection(`configs`).doc(changeValue?.form?.rawId).update({
        ...configPacks,
        iap: {
          ...configPacks?.iap || {},
          [changeValue?.form?.type]: changeValue?.value
        }
      }).then((result) => {
        notification.info({
          description: `Package was updated successfully`,
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
            description: `Package was updated failure`,
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
        setConfigPacks(docs?.[0]?.data?.())
        handleAdsData(docs?.[0]?.data?.()?.iap, docs?.[0]?.id)
      })
      .catch()
  }

  const handleAdsData = (rawPack = {}, rawId) => {

    const newPacks = Object.entries(rawPack)?.reduce((arr, item) => ([...arr, {
      type: item?.[0],
      value: item?.[1],
      rawId,
      id: v4()
    }
    ]), [])

    setPack(newPacks)
  }

  const currentUser = auth?.currentUser
  const isAllow = (currentUser?.email && currentUser?.email?.toLowerCase() !== EMAIL) || (userInfo?.email && userInfo?.email !== EMAIL)

  return (
    <Layout title="Packages">
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
          customComp={{
            value: ({text, row}) => (
              <div className="text-left">
                <input
                  onChange={({target: {value}}) => isAllow &&
                    setChangeValue({
                      value,
                      id: `${row?.id}`,
                      form: row,
                    })
                  }
                  value={changeValue?.id === `${row?.id}` ? changeValue?.value : text}
                  placeholder="Show"
                  className="w-full outline-none px-2 py-1"/>
              </div>
            ),
          }}
          staticData={packs}
          isHiddenPg={false}
          defineCols={[
            {
              name: () => <div className="text-left">Purchase Id</div>,
              code: "value",
              sort: 1,
            },
            {
              name: () => <div className="text-center">duration</div>,
              code: "type",
              sort: 2
            },
          ]}
          payload={{}}
          headerWidth={{
            action: 92,
            adsName: 989,
            value: 200,
          }}
          columns={[]}
        />
      </div>
    </Layout>
  )
}